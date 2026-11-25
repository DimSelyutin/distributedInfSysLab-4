package by.gstu.lab4.service.pipeline;

import by.gstu.lab4.model.ProcessedTile;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Сборщик результатов: складывает обработанные фрагменты под критической секцией.
 */
@Log4j2
public class ResultCollector {

    private final BufferedImage canvas;
    private final List<ProcessedTile> accepted = new ArrayList<>();
    @Getter
    private int acceptedCount;

    public ResultCollector(int width, int height) {
        this.canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Критическая секция: запись фрагмента в общий буфер и журнал результатов.
     */
    public synchronized void accept(ProcessedTile tile) {
        canvas.setRGB(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight(),
                tile.getPixels(), 0, tile.getWidth());
        accepted.add(tile);
        acceptedCount++;
        log.debug("Collected tile #{} at ({},{})", tile.getIndex(), tile.getX(), tile.getY());
    }

    public synchronized BufferedImage snapshot() {
        BufferedImage copy = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        copy.getGraphics().drawImage(canvas, 0, 0, null);
        copy.getGraphics().dispose();
        return copy;
    }

    public synchronized List<ProcessedTile> getAccepted() {
        return List.copyOf(accepted);
    }
}
