package by.gstu.lab4.service.processing;

import by.gstu.lab4.model.ImageTile;
import by.gstu.lab4.model.ProcessedTile;

@FunctionalInterface
public interface TileFilter {
    ProcessedTile apply(ImageTile tile);
}
