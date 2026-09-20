package org.example.vvpd.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.vvpd.task.model.TaskPriority;

public record CreateTaskRequest(
        @NotBlank
        @Size(max = 120)
        String title,

        @NotBlank
        @Size(max = 2000)
        String description,

        @NotNull
        TaskPriority priority
) {
}
