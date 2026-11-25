package by.gstu.lab4.model;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;

@Value
@Builder
public class ProcessingRequest {
    Path inputPath;
    Path outputPath;
    @Builder.Default
    FilterType filterType = FilterType.SWAP_RB;
    @Builder.Default
    int poolSize = Runtime.getRuntime().availableProcessors();
    @Builder.Default
    int tileHeight = 32;
}
