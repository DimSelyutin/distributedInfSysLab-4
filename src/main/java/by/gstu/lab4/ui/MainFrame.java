package by.gstu.lab4.ui;

import by.gstu.lab4.model.FilterType;
import by.gstu.lab4.model.ProcessingRequest;
import by.gstu.lab4.model.ProcessingStats;
import by.gstu.lab4.service.ImageProcessingService;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Log4j2
public class MainFrame extends JFrame {

    private final ImagePanel sourcePanel = new ImagePanel();
    private final ImagePanel resultPanel = new ImagePanel();
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel statusLabel = new JLabel("Готово");
    private final JSpinner poolSizeSpinner = new JSpinner(
            new SpinnerNumberModel(Runtime.getRuntime().availableProcessors(), 1, 64, 1));
    private final JSpinner tileHeightSpinner = new JSpinner(new SpinnerNumberModel(32, 1, 1024, 1));

    private final JButton openButton = new JButton("Открыть…");
    private final JButton startButton = new JButton("Обработать");
    private final JButton cancelButton = new JButton("Отмена");
    private final JButton saveButton = new JButton("Сохранить как…");

    private Path inputPath;
    private Path lastOutputPath;
    private ImageProcessingService processingService;
    private volatile boolean running;

    public MainFrame() {
        super("ЛР4 · Вариант 16 · Обмен каналов R↔B");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildImages(), BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        wireActions();
        setRunning(false);
    }

    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(openButton);
        panel.add(startButton);
        panel.add(cancelButton);
        panel.add(saveButton);
        panel.add(new JLabel("Пул потоков:"));
        panel.add(poolSizeSpinner);
        panel.add(new JLabel("Высота тайла:"));
        panel.add(tileHeightSpinner);
        panel.add(new JLabel(FilterType.SWAP_RB.toString()));
        return panel;
    }

    private JPanel buildImages() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 8));
        sourcePanel.setBorder(BorderFactory.createTitledBorder("Исходное"));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Результат (R↔B)"));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(sourcePanel);
        panel.add(resultPanel);
        return panel;
    }

    private JPanel buildStatus() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void wireActions() {
        openButton.addActionListener(e -> openImage());
        startButton.addActionListener(e -> startProcessing());
        cancelButton.addActionListener(e -> {
            if (processingService != null) {
                processingService.cancel();
                statusLabel.setText("Отмена…");
            }
        });
        saveButton.addActionListener(e -> saveAs());
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Изображения", "png", "jpg", "jpeg", "bmp", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) {
                JOptionPane.showMessageDialog(this, "Не удалось прочитать изображение", "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            inputPath = path;
            sourcePanel.setImage(image);
            resultPanel.setImage(null);
            progressBar.setValue(0);
            statusLabel.setText("Загружено: " + path.getFileName());
            log.info("UI opened image {}", path);
        } catch (IOException ex) {
            log.error("Failed to open image", ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startProcessing() {
        if (inputPath == null) {
            JOptionPane.showMessageDialog(this, "Сначала откройте изображение", "Внимание",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Path output = resolveDefaultOutput(inputPath);
        lastOutputPath = output;
        processingService = new ImageProcessingService();
        ProcessingRequest request = ProcessingRequest.builder()
                .inputPath(inputPath)
                .outputPath(output)
                .filterType(FilterType.SWAP_RB)
                .poolSize((Integer) poolSizeSpinner.getValue())
                .tileHeight((Integer) tileHeightSpinner.getValue())
                .build();

        setRunning(true);
        progressBar.setValue(0);
        statusLabel.setText("Обработка…");

        SwingWorker<ProcessingStats, Void> worker = new SwingWorker<>() {
            @Override
            protected ProcessingStats doInBackground() {
                return processingService.processAsync(
                        request,
                        percent -> SwingUtilities.invokeLater(() -> progressBar.setValue(percent)),
                        preview -> SwingUtilities.invokeLater(() -> resultPanel.setImage(preview))
                ).join();
            }

            @Override
            protected void done() {
                setRunning(false);
                try {
                    ProcessingStats stats = get();
                    if (stats.isCancelled()) {
                        progressBar.setValue(0);
                        statusLabel.setText("Отменено. Результат не сохранён.");
                        resultPanel.setImage(null);
                    } else {
                        progressBar.setValue(100);
                        statusLabel.setText(String.format(
                                "Готово · read=%d ms, process=%d ms, write=%d ms, total=%d ms, tiles=%d, pool=%d → %s",
                                stats.getReadMillis(), stats.getProcessMillis(), stats.getWriteMillis(),
                                stats.getTotalMillis(), stats.getTileCount(), stats.getPoolSize(),
                                lastOutputPath.getFileName()));
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Ошибка: " + ex.getCause());
                    log.error("Processing failed", ex);
                }
            }
        };
        worker.execute();
    }

    private void saveAs() {
        BufferedImage image = resultPanel.getImage();
        if (image == null) {
            JOptionPane.showMessageDialog(this, "Нет результата для сохранения", "Внимание",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(lastOutputPath != null
                ? lastOutputPath.toFile()
                : new java.io.File("result_swap_rb.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        try {
            String name = path.getFileName().toString().toLowerCase();
            String format = name.endsWith(".jpg") || name.endsWith(".jpeg") ? "jpg" : "png";
            ImageIO.write(image, format, path.toFile());
            lastOutputPath = path;
            statusLabel.setText("Сохранено: " + path);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setRunning(boolean value) {
        running = value;
        openButton.setEnabled(!value);
        startButton.setEnabled(!value);
        cancelButton.setEnabled(value);
        saveButton.setEnabled(!value && resultPanel.getImage() != null);
        poolSizeSpinner.setEnabled(!value);
        tileHeightSpinner.setEnabled(!value);
    }

    private static Path resolveDefaultOutput(Path input) {
        String name = input.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        Path parent = input.getParent() != null ? input.getParent() : Path.of(".");
        return parent.resolve(base + "_swap_rb.png");
    }
}
