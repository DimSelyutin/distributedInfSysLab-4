package by.gstu.lab4.service.pipeline;

import by.gstu.lab4.model.ProcessedTile;
import by.gstu.lab4.service.processing.PixelOps;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultCollectorTest {

    @Test
    void accept_writesTileIntoSharedCanvasUnderCriticalSection() {
        ResultCollector collector = new ResultCollector(4, 2);
        int[] pixels = {
                PixelOps.argb(255, 1, 2, 3),
                PixelOps.argb(255, 4, 5, 6),
                PixelOps.argb(255, 7, 8, 9),
                PixelOps.argb(255, 10, 11, 12)
        };
        collector.accept(ProcessedTile.builder()
                .index(0).x(0).y(0).width(4).height(1).pixels(pixels).build());

        BufferedImage image = collector.snapshot();
        assertEquals(1, collector.getAcceptedCount());
        assertEquals(PixelOps.argb(255, 1, 2, 3), image.getRGB(0, 0));
        assertEquals(PixelOps.argb(255, 10, 11, 12), image.getRGB(3, 0));
    }
}
