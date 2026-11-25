package by.gstu.lab4.service.pipeline;

import lombok.Getter;

import java.util.function.IntConsumer;

/**
 * Счётчик прогресса, защищённый критической секцией (synchronized).
 */
public class ProgressTracker {

    private final int total;
    private final IntConsumer listener;
    @Getter
    private int completed;

    public ProgressTracker(int total, IntConsumer listener) {
        this.total = Math.max(0, total);
        this.listener = listener;
    }

    /**
     * Критическая секция: обновление счётчика завершённых фрагментов.
     */
    public synchronized void increment() {
        completed++;
        int percent = total == 0 ? 100 : (int) Math.min(100, (completed * 100L) / total);
        if (listener != null) {
            listener.accept(percent);
        }
    }

    public synchronized int getPercent() {
        return total == 0 ? 100 : (int) Math.min(100, (completed * 100L) / total);
    }
}
