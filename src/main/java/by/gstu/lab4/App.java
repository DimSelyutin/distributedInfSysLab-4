package by.gstu.lab4;

import by.gstu.lab4.ui.MainFrame;
import lombok.extern.log4j.Log4j2;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Лабораторная работа 4 — вариант 16:
 * асинхронная многопоточная обработка изображений (обмен каналов R↔B).
 */
@Log4j2
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        log.info("Starting Lab 4, variant 16 (Swap R↔B)");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // keep default L&F
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
