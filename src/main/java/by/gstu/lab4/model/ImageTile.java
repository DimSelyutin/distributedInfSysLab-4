package by.gstu.lab4.model;

import lombok.Builder;
import lombok.Value;

/**
 * Фрагмент изображения для независимой обработки.
 */
@Value
@Builder
public class ImageTile {
    int index;
    int x;
    int y;
    int width;
    int height;
    /** Пиксели фрагмента в формате ARGB (ширина × высота). */
    int[] pixels;
}
