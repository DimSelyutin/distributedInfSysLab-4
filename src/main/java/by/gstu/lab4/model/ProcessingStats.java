package by.gstu.lab4.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProcessingStats {
    long readMillis;
    long processMillis;
    long writeMillis;
    long totalMillis;
    int tileCount;
    int poolSize;
    boolean cancelled;
}
