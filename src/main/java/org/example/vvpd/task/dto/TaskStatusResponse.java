package org.example.vvpd.task.dto;

import java.util.UUID;
import org.example.vvpd.task.model.TaskStatus;

public record TaskStatusResponse(
        UUID id,
        TaskStatus status
) {
}
