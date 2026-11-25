package by.gstu.lab4.service.processing;

import by.gstu.lab4.model.ImageTile;
import by.gstu.lab4.model.ProcessedTile;
import lombok.extern.log4j.Log4j2;

/**
 * Вариант 16: замена каждого пикселя ARGB → ABGR (R↔B).
 */
@Log4j2
public class SwapRedBlueFilter implements TileFilter {

    @Override
    public ProcessedTile apply(ImageTile tile) {
        int[] source = tile.getPixels();
        int[] result = new int[source.length];
        for (int i = 0; i < source.length; i++) {
            result[i] = PixelOps.swapRedBlue(source[i]);
        }
        log.trace("Tile #{} processed ({} px)", tile.getIndex(), source.length);
        return ProcessedTile.builder()
                .index(tile.getIndex())
                .x(tile.getX())
                .y(tile.getY())
                .width(tile.getWidth())
                .height(tile.getHeight())
                .pixels(result)
                .build();
    }
}
