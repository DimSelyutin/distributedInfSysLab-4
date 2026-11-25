package by.gstu.lab4.service.io;

import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Асинхронное чтение файла изображения (не блокирует UI-поток).
 */
@Log4j2
public class AsyncImageReader {

    public CompletableFuture<BufferedImage> readAsync(Path path, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
                    log.info("Reading image asynchronously: {}", path.toAbsolutePath());
                    try (var in = Files.newInputStream(path)) {
                        BufferedImage image = ImageIO.read(in);
                        if (image == null) {
                            throw new IllegalArgumentException("Unsupported or corrupted image: " + path);
                        }
                        BufferedImage argb = new BufferedImage(
                                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        argb.getGraphics().drawImage(image, 0, 0, null);
                        argb.getGraphics().dispose();
                        log.info("Image loaded: {}×{}", argb.getWidth(), argb.getHeight());
                        return argb;
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to read image: " + path, e);
                    }
        }, executor);
    }
}
