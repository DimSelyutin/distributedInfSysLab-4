package by.gstu.lab4.service.pipeline;

import by.gstu.lab4.model.ImageTile;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Делит изображение на горизонтальные полосы и формирует задания.
 */
@Log4j2
public class TileScheduler {

    public List<ImageTile> split(BufferedImage image, int tileHeight) {
        int width = image.getWidth();
        int height = image.getHeight();
        int stripHeight = Math.max(1, tileHeight);
        List<ImageTile> tiles = new ArrayList<>();
        int index = 0;

        for (int y = 0; y < height; y += stripHeight) {
            int h = Math.min(stripHeight, height - y);
            int[] pixels = new int[width * h];
            image.getRGB(0, y, width, h, pixels, 0, width);
            tiles.add(ImageTile.builder()
                    .index(index++)
                    .x(0)
                    .y(y)
                    .width(width)
                    .height(h)
                    .pixels(pixels)
                    .build());
        }

        log.info("Image {}×{} split into {} tiles (tileHeight={})", width, height, tiles.size(), stripHeight);
        return tiles;
    }
}
