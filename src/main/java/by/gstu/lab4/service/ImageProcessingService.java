package by.gstu.lab4.service;

import by.gstu.lab4.model.ProcessingRequest;
import by.gstu.lab4.model.ProcessingStats;
import by.gstu.lab4.service.io.AsyncImageReader;
import by.gstu.lab4.service.io.AsyncImageWriter;
import by.gstu.lab4.service.pipeline.ImageProcessingPipeline;
import by.gstu.lab4.service.pipeline.TileScheduler;
import by.gstu.lab4.service.processing.SwapRedBlueFilter;
import by.gstu.lab4.service.processing.TileFilter;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Сервисный слой: фасад асинхронной многопоточной обработки изображений (вариант 16).
 */
@Log4j2
public class ImageProcessingService {

    private final ImageProcessingPipeline pipeline;

    public ImageProcessingService() {
        this(new AsyncImageReader(), new AsyncImageWriter(), new TileScheduler(), new SwapRedBlueFilter());
    }

    public ImageProcessingService(
            AsyncImageReader reader,
            AsyncImageWriter writer,
            TileScheduler scheduler,
            TileFilter filter) {
        this.pipeline = new ImageProcessingPipeline(reader, writer, scheduler, filter);
    }

    public CompletableFuture<ProcessingStats> processAsync(
            ProcessingRequest request,
            IntConsumer progressListener,
            Consumer<BufferedImage> previewListener) {
        log.info("Service: start processing {}", request.getInputPath());
        return pipeline.processAsync(request, progressListener, previewListener);
    }

    public void cancel() {
        log.info("Service: cancel requested");
        pipeline.cancel();
    }
}
