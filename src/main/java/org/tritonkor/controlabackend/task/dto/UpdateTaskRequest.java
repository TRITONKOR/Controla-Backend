package org.tritonkor.controlabackend.task.dto;

import jakarta.validation.constraints.Size;
import org.tritonkor.controlabackend.task.entity.TaskStatus;


public record UpdateTaskRequest(
        @Size(max = 255, message = "Назва завдання не може бути довше 255 символів")
        String title,

        @Size(max = 1000, message = "Опис завдання не може бути довше 1000 символів")
        String description,

        TaskStatus status,

        String attachmentUrl,
        String attachmentName
) {
}
