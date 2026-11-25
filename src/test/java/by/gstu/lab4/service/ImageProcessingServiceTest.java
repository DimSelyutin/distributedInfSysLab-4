package by.gstu.lab4.service;

import by.gstu.lab4.model.ProcessingRequest;
import by.gstu.lab4.model.ProcessingStats;
import by.gstu.lab4.service.io.AsyncImageReader;
import by.gstu.lab4.service.io.AsyncImageWriter;
import by.gstu.lab4.service.pipeline.TileScheduler;
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
class ImageProcessingServiceTest {

    @Mock
    private AsyncImageReader reader;

    @Mock
    private AsyncImageWriter writer;

    @TempDir
    Path tempDir;

    @Test
    void processAsync_delegatesToPipelineAndSwapsChannels() throws Exception {
        BufferedImage source = new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, PixelOps.argb(255, 200, 50, 10));

        Path input = tempDir.resolve("in.png");
        Path output = tempDir.resolve("out.png");
        ImageIO.write(source, "png", input.toFile());

        when(reader.readAsync(any(), any())).thenAnswer(inv -> {
            Executor executor = inv.getArgument(1);
            return CompletableFuture.supplyAsync(() -> source, executor);
        });
        when(writer.writeAsync(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        ImageProcessingService service = new ImageProcessingService(
                reader, writer, new TileScheduler(), new SwapRedBlueFilter());

        ProcessingStats stats = service.processAsync(
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
        ArgumentCaptor<BufferedImage> imageCaptor = ArgumentCaptor.forClass(BufferedImage.class);
        verify(writer).writeAsync(imageCaptor.capture(), any(), any());
        assertEquals(PixelOps.argb(255, 10, 50, 200), imageCaptor.getValue().getRGB(0, 0));
    }
}
