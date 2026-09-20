package org.example.vvpd.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "task")
public record TaskProcessingProperties(
        int workers,
        Duration processingDuration
) {

    public TaskProcessingProperties {
        if (workers < 1) {
            workers = 1;
        }
        if (processingDuration == null || processingDuration.isNegative()) {
            processingDuration = Duration.ZERO;
        }
    }
}
