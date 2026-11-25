package by.gstu.lab4.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Вариант 16: обмен цветовых каналов R↔B с сохранением альфа-канала.
 */
@Getter
@RequiredArgsConstructor
public enum FilterType {
    SWAP_RB(16, "Обмен каналов R↔B");

    private final int variant;
    private final String title;

    @Override
    public String toString() {
        return "Вариант " + variant + ": " + title;
    }
}
