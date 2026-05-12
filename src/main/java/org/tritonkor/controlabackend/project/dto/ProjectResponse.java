package org.tritonkor.controlabackend.project.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectResponse (
        UUID id,
        String title,
        String description,
        UUID ownerId,
        String ownerFirstName,
        String ownerLastName,
        String ownerAvatar,
        String status,
        Integer costs,
        LocalDateTime deadline,
        List<TaskShortResponse> tasks,
        List<EmployeeShortResponse> assignees
) {
    public record TaskShortResponse(
            String id,
            String title,
            String description,
            String status
    ) {}

    public record EmployeeShortResponse(
            String id,
            String userId,
            String firstName,
            String lastName,
            String departmentTitle
    ) {}
}



