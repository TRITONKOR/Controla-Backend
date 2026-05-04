package org.tritonkor.controlabackend.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.tritonkor.controlabackend.task.entity.TaskStatus;

import java.util.UUID;

public record CreateTaskRequest(
        @NotBlank(message = "Назва завдання не може бути порожньою")
        @Size(max = 255, message = "Назва завдання не може бути довше 255 символів")
        String title,

        @NotBlank(message = "Опис завдання не може бути порожнім")
        @Size(max = 1000, message = "Опис завдання не може бути довше 1000 символів")
        String description,

        @NotNull(message = "Статус завдання не може бути null")
        TaskStatus status,

        @NotNull(message = "ID проекту не може бути null")
        UUID projectId,

        String attachments
) {
}
