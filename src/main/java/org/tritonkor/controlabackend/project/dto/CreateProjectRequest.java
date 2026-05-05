package org.tritonkor.controlabackend.project.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateProjectRequest(
        String title,
        String description,
        Integer costs,
        LocalDateTime deadline
) {
}
