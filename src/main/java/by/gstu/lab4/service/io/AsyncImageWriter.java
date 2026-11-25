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
 * Асинхронная запись результата на диск.
 */
@Log4j2
public class AsyncImageWriter {

    public CompletableFuture<Void> writeAsync(BufferedImage image, Path path, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            log.info("Writing image asynchronously: {}", path.toAbsolutePath());
            try {
                Path parent = path.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                String format = resolveFormat(path);
                try (var out = Files.newOutputStream(path)) {
                    if (!ImageIO.write(image, format, out)) {
                        throw new IllegalStateException("No ImageIO writer for format: " + format);
                    }
                }
                log.info("Image saved: {}", path.toAbsolutePath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write image: " + path, e);
            }
        }, executor);
    }

    private static String resolveFormat(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "jpg";
        }
        if (name.endsWith(".bmp")) {
            return "bmp";
        }
        if (name.endsWith(".gif")) {
            return "gif";
        }
        return "png";
    }
}
