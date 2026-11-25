package by.gstu.lab4.service.processing;

import lombok.experimental.UtilityClass;

import java.awt.Color;

@UtilityClass
public class PixelOps {

    public int alpha(int argb) {
        return new Color(argb, true).getAlpha();
    }

    public int red(int argb) {
        return new Color(argb, true).getRed();
    }

    public int green(int argb) {
        return new Color(argb, true).getGreen();
    }

    public int blue(int argb) {
        return new Color(argb, true).getBlue();
    }

    public int argb(int a, int r, int g, int b) {
        return new Color(r, g, b, a).getRGB();
    }

    /**
     * Обмен каналов R↔B, альфа и зелёный без изменений.
     */
    public int swapRedBlue(int argb) {
        Color color = new Color(argb, true);
        return new Color(color.getBlue(), color.getGreen(), color.getRed(), color.getAlpha()).getRGB();
    }
}
