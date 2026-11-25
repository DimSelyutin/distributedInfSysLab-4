package by.gstu.lab4.service.pipeline;

import by.gstu.lab4.service.io.AsyncImageReader;
import by.gstu.lab4.service.io.AsyncImageWriter;
import by.gstu.lab4.model.ImageTile;
import by.gstu.lab4.model.ProcessedTile;
import by.gstu.lab4.model.ProcessingRequest;
import by.gstu.lab4.model.ProcessingStats;
import by.gstu.lab4.service.processing.SwapRedBlueFilter;
import by.gstu.lab4.service.processing.TileFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Конвейер обработки:
 * UI → async reader → scheduler → queue → worker pool → collector → async writer.
 * <p>
 * Синхронизация:
 * <ul>
 *   <li>{@link BlockingQueue} — очередь заданий (сигнал «появилось задание»);</li>
 *   <li>{@link Semaphore} — ограничение числа одновременно обрабатываемых фрагментов;</li>
 *   <li>{@link CountDownLatch} — ожидание завершения всех фрагментов;</li>
 *   <li>synchronized в {@link ResultCollector} / {@link ProgressTracker} — критические секции.</li>
 * </ul>
 */
@Log4j2
@RequiredArgsConstructor
public class ImageProcessingPipeline {

    private static final ImageTile POISON = ImageTile.builder()
            .index(-1).x(0).y(0).width(0).height(0).pixels(new int[0]).build();

    private final AsyncImageReader reader;
    private final AsyncImageWriter writer;
    private final TileScheduler scheduler;
    private final TileFilter filter;

    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private volatile ExecutorService workers;

    public ImageProcessingPipeline() {
        this(new AsyncImageReader(), new AsyncImageWriter(), new TileScheduler(), new SwapRedBlueFilter());
    }

    public void cancel() {
        cancelRequested.set(true);
        ExecutorService pool = workers;
        if (pool != null) {
            pool.shutdownNow();
        }
        log.info("Cancellation requested");
    }

    public CompletableFuture<ProcessingStats> processAsync(
            ProcessingRequest request,
            IntConsumer progressListener,
            Consumer<BufferedImage> previewListener) {

        cancelRequested.set(false);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "async-io");
            t.setDaemon(true);
            return t;
        });

        long totalStart = System.nanoTime();

        CompletableFuture<ProcessingStats> future = reader.readAsync(request.getInputPath(), ioExecutor)
                .thenCompose(image -> {
                    if (cancelRequested.get()) {
                        return CompletableFuture.completedFuture(
                                stats(0, 0, 0, 0, request.getPoolSize(), true));
                    }
                    long readMillis = elapsedMillis(totalStart);

                    return CompletableFuture.supplyAsync(() -> {
                        ProcessOutcome outcome = processImage(
                                image, request, progressListener, previewListener);
                        long processMillis = outcome.processMillis();
                        if (outcome.cancelled() || cancelRequested.get()) {
                            return stats(readMillis, processMillis, 0, outcome.tileCount(),
                                    request.getPoolSize(), true);
                        }

                        long writeStart = System.nanoTime();
                        try {
                            writer.writeAsync(outcome.image(), request.getOutputPath(), Runnable::run).join();
                        } catch (Exception e) {
                            throw new IllegalStateException("Write failed", e);
                        }
                        long writeMillis = elapsedMillis(writeStart);
                        long totalMillis = elapsedMillis(totalStart);
                        ProcessingStats result = ProcessingStats.builder()
                                .readMillis(readMillis)
                                .processMillis(processMillis)
                                .writeMillis(writeMillis)
                                .totalMillis(totalMillis)
                                .tileCount(outcome.tileCount())
                                .poolSize(request.getPoolSize())
                                .cancelled(false)
                                .build();
                        log.info("Pipeline finished: {}", result);
                        return result;
                    }, ioExecutor);
                });

        future.whenComplete((s, e) -> ioExecutor.shutdown());
        return future;
    }

    private ProcessOutcome processImage(
            BufferedImage image,
            ProcessingRequest request,
            IntConsumer progressListener,
            Consumer<BufferedImage> previewListener) {

        long processStart = System.nanoTime();
        List<ImageTile> tiles = scheduler.split(image, request.getTileHeight());
        int tileCount = tiles.size();
        ProgressTracker progress = new ProgressTracker(tileCount, progressListener);
        ResultCollector collector = new ResultCollector(image.getWidth(), image.getHeight());

        BlockingQueue<ImageTile> queue = new ArrayBlockingQueue<>(Math.max(tileCount + request.getPoolSize(), 1));
        Semaphore concurrency = new Semaphore(Math.max(1, request.getPoolSize()));
        CountDownLatch done = new CountDownLatch(tileCount);

        int poolSize = Math.max(1, request.getPoolSize());
        workers = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "tile-worker");
            t.setDaemon(true);
            return t;
        });

        for (int i = 0; i < poolSize; i++) {
            workers.submit(() -> workerLoop(queue, concurrency, collector, progress, done, previewListener));
        }

        int submitted = 0;
        try {
            for (ImageTile tile : tiles) {
                if (cancelRequested.get()) {
                    break;
                }
                queue.put(tile);
                submitted++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelRequested.set(true);
        } finally {
            // Фрагменты, не попавшие в очередь, сразу снимаем с latch
            for (int i = submitted; i < tileCount; i++) {
                done.countDown();
            }
            for (int i = 0; i < poolSize; i++) {
                queue.offer(POISON);
            }
        }

        try {
            long deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(5);
            while (done.getCount() > 0) {
                if (cancelRequested.get() || System.nanoTime() > deadline) {
                    break;
                }
                done.await(100, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelRequested.set(true);
        }

        releaseRemaining(done);
        shutdownWorkers();

        long processMillis = elapsedMillis(processStart);
        boolean cancelled = cancelRequested.get() || collector.getAcceptedCount() < tileCount;

        if (cancelled) {
            log.warn("Processing cancelled (accepted {}/{})", collector.getAcceptedCount(), tileCount);
            return new ProcessOutcome(null, processMillis, tileCount, true);
        }

        BufferedImage result = collector.snapshot();
        if (previewListener != null) {
            previewListener.accept(result);
        }
        return new ProcessOutcome(result, processMillis, tileCount, false);
    }

    private void workerLoop(
            BlockingQueue<ImageTile> queue,
            Semaphore concurrency,
            ResultCollector collector,
            ProgressTracker progress,
            CountDownLatch done,
            Consumer<BufferedImage> previewListener) {

        try {
            while (!Thread.currentThread().isInterrupted()) {
                ImageTile tile = queue.take();
                if (tile == POISON || tile.getIndex() < 0) {
                    break;
                }
                if (cancelRequested.get()) {
                    done.countDown();
                    continue;
                }
                concurrency.acquire();
                try {
                    if (cancelRequested.get()) {
                        done.countDown();
                        continue;
                    }
                    ProcessedTile processed = filter.apply(tile);
                    collector.accept(processed);
                    progress.increment();
                    if (previewListener != null && progress.getCompleted() % 4 == 0) {
                        previewListener.accept(collector.snapshot());
                    }
                    done.countDown();
                } finally {
                    concurrency.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Worker interrupted");
        }
    }

    private void releaseRemaining(CountDownLatch done) {
        while (done.getCount() > 0) {
            done.countDown();
        }
    }

    private void shutdownWorkers() {
        ExecutorService pool = workers;
        if (pool == null) {
            return;
        }
        pool.shutdownNow();
        try {
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            workers = null;
        }
    }

    private static long elapsedMillis(long startNano) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
    }

    private static ProcessingStats stats(
            long read, long process, long write, int tiles, int poolSize, boolean cancelled) {
        return ProcessingStats.builder()
                .readMillis(read)
                .processMillis(process)
                .writeMillis(write)
                .totalMillis(read + process + write)
                .tileCount(tiles)
                .poolSize(poolSize)
                .cancelled(cancelled)
                .build();
    }

    private record ProcessOutcome(BufferedImage image, long processMillis, int tileCount, boolean cancelled) {
    }
}
