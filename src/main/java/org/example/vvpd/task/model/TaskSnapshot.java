package org.example.vvpd.task.model;

import java.time.Instant;
import java.util.UUID;

public record TaskSnapshot(
        UUID id,
        String title,
        String description,
        TaskPriority priority,
        TaskStatus status,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt,
        String result,
        String error
) {
}
