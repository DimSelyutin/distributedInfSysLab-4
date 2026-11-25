package by.gstu.lab4.service.processing;

import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PixelOpsTest {

    @Test
    void swapRedBlue_preservesAlphaAndGreen() {
        int argb = new Color(0x11, 0x22, 0x33, 0xAB).getRGB();
        int swapped = PixelOps.swapRedBlue(argb);
        Color result = new Color(swapped, true);

        assertEquals(0xAB, result.getAlpha());
        assertEquals(0x33, result.getRed());
        assertEquals(0x22, result.getGreen());
        assertEquals(0x11, result.getBlue());
    }

    @Test
    void swapRedBlue_isInvolution() {
        int argb = new Color(10, 20, 30, 255).getRGB();
        assertEquals(argb, PixelOps.swapRedBlue(PixelOps.swapRedBlue(argb)));
    }
}
