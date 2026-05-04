package org.tritonkor.controlabackend.task.dto;

import jakarta.validation.constraints.NotBlank;
import org.tritonkor.controlabackend.task.entity.TaskStatus;

public record TaskStatusUpdateRequest(
        @NotBlank(message = "Статус не можу бути порожнім")
        TaskStatus status
) {
}
