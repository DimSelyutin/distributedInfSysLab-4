package by.gstu.lab4.service.pipeline;

import by.gstu.lab4.service.io.AsyncImageReader;
import by.gstu.lab4.service.io.AsyncImageWriter;
import by.gstu.lab4.model.ProcessingRequest;
import by.gstu.lab4.model.ProcessingStats;
import by.gstu.lab4.service.processing.PixelOps;
import by.gstu.lab4.service.processing.SwapRedBlueFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageProcessingPipelineTest {

    @Mock
    private AsyncImageReader reader;

    @Mock
    private AsyncImageWriter writer;

    @TempDir
    Path tempDir;

    @Test
    void processAsync_swapsChannelsAndWritesResult() throws Exception {
        BufferedImage source = new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, PixelOps.argb(255, 200, 50, 10));
        source.setRGB(7, 5, PixelOps.argb(255, 1, 2, 3));

        Path input = tempDir.resolve("in.png");
        Path output = tempDir.resolve("out.png");
        ImageIO.write(source, "png", input.toFile());

        when(reader.readAsync(any(), any())).thenAnswer(inv -> {
            Executor executor = inv.getArgument(1);
            return CompletableFuture.supplyAsync(() -> source, executor);
        });
        when(writer.writeAsync(any(), any(), any())).thenAnswer(inv -> {
            BufferedImage image = inv.getArgument(0);
            Path path = inv.getArgument(1);
            Executor executor = inv.getArgument(2);
            return CompletableFuture.runAsync(() -> {
                try {
                    ImageIO.write(image, "png", path.toFile());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }, executor);
        });

        ImageProcessingPipeline pipeline = new ImageProcessingPipeline(
                reader, writer, new TileScheduler(), new SwapRedBlueFilter());

        ProcessingStats stats = pipeline.processAsync(
                ProcessingRequest.builder()
                        .inputPath(input)
                        .outputPath(output)
                        .poolSize(2)
                        .tileHeight(2)
                        .build(),
                percent -> {
                },
                preview -> {
                }
        ).get();

        assertFalse(stats.isCancelled());
        assertEquals(3, stats.getTileCount());

        ArgumentCaptor<BufferedImage> imageCaptor = ArgumentCaptor.forClass(BufferedImage.class);
        verify(writer).writeAsync(imageCaptor.capture(), any(), any());
        BufferedImage result = imageCaptor.getValue();

        assertEquals(PixelOps.argb(255, 10, 50, 200), result.getRGB(0, 0));
        assertEquals(PixelOps.argb(255, 3, 2, 1), result.getRGB(7, 5));
    }
}
