package by.gstu.lab4.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProcessedTile {
    int index;
    int x;
    int y;
    int width;
    int height;
    int[] pixels;
}
