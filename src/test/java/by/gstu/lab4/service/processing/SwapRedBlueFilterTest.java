package by.gstu.lab4.service.processing;

import by.gstu.lab4.model.ImageTile;
import by.gstu.lab4.model.ProcessedTile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SwapRedBlueFilterTest {

    private final SwapRedBlueFilter filter = new SwapRedBlueFilter();

    @Test
    void apply_swapsChannelsForAllPixels() {
        int[] pixels = {
                PixelOps.argb(255, 255, 0, 0),
                PixelOps.argb(128, 0, 255, 0),
                PixelOps.argb(255, 0, 0, 255)
        };
        ImageTile tile = ImageTile.builder()
                .index(0).x(0).y(0).width(3).height(1).pixels(pixels).build();

        ProcessedTile result = filter.apply(tile);

        assertEquals(0, result.getIndex());
        assertEquals(3, result.getWidth());
        assertArrayEquals(new int[]{
                PixelOps.argb(255, 0, 0, 255),
                PixelOps.argb(128, 0, 255, 0),
                PixelOps.argb(255, 255, 0, 0)
        }, result.getPixels());
    }
}
