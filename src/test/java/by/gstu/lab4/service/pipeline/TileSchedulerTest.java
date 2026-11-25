package by.gstu.lab4.service.pipeline;

import by.gstu.lab4.model.ImageTile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TileSchedulerTest {

    @Test
    void split_createsHorizontalStrips() {
        BufferedImage image = new BufferedImage(10, 25, BufferedImage.TYPE_INT_ARGB);
        TileScheduler scheduler = new TileScheduler();

        List<ImageTile> tiles = scheduler.split(image, 10);

        assertEquals(3, tiles.size());
        assertEquals(10, tiles.get(0).getHeight());
        assertEquals(10, tiles.get(1).getHeight());
        assertEquals(5, tiles.get(2).getHeight());
        assertEquals(20, tiles.get(2).getY());
    }
}
