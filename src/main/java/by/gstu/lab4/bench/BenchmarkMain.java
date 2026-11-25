package by.gstu.lab4.bench;

import by.gstu.lab4.model.ProcessingRequest;
import by.gstu.lab4.model.ProcessingStats;
import by.gstu.lab4.service.ImageProcessingService;
import by.gstu.lab4.service.processing.PixelOps;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Замер времени обработки при разном размере пула (для отчёта).
 */
public final class BenchmarkMain {

    private BenchmarkMain() {
    }

    public static void main(String[] args) throws Exception {
        Path dir = Path.of("target", "bench");
        Files.createDirectories(dir);
        Path input = dir.resolve("bench_in.png");
        Path output = dir.resolve("bench_out.png");

        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, PixelOps.argb(255, x % 256, y % 256, (x + y) % 256));
            }
        }
        ImageIO.write(image, "png", input.toFile());

        int[] pools = {1, 2, 4, 8};
        System.out.println("pool;readMs;processMs;writeMs;totalMs;tiles");
        for (int pool : pools) {
            long best = Long.MAX_VALUE;
            ProcessingStats bestStats = null;
            for (int i = 0; i < 3; i++) {
                ImageProcessingService service = new ImageProcessingService();
                ProcessingStats stats = service.processAsync(
                        ProcessingRequest.builder()
                                .inputPath(input)
                                .outputPath(output)
                                .poolSize(pool)
                                .tileHeight(32)
                                .build(),
                        p -> {
                        },
                        img -> {
                        }
                ).get();
                if (!stats.isCancelled() && stats.getTotalMillis() < best) {
                    best = stats.getTotalMillis();
                    bestStats = stats;
                }
            }
            System.out.printf("%d;%d;%d;%d;%d;%d%n",
                    bestStats.getPoolSize(),
                    bestStats.getReadMillis(),
                    bestStats.getProcessMillis(),
                    bestStats.getWriteMillis(),
                    bestStats.getTotalMillis(),
                    bestStats.getTileCount());
        }
    }
}
