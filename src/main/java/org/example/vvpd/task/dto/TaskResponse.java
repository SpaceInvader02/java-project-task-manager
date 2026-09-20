package org.example.vvpd.task.dto;

import java.time.Instant;
import java.util.UUID;
import org.example.vvpd.task.model.TaskPriority;
import org.example.vvpd.task.model.TaskSnapshot;
import org.example.vvpd.task.model.TaskStatus;

public record TaskResponse(
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

    public static TaskResponse from(TaskSnapshot task) {
        return new TaskResponse(
                task.id(),
                task.title(),
                task.description(),
                task.priority(),
                task.status(),
                task.createdAt(),
                task.startedAt(),
                task.completedAt(),
                task.result(),
                task.error()
        );
    }
}
