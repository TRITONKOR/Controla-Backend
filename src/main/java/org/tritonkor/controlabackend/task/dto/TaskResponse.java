package org.tritonkor.controlabackend.task.dto;

import java.util.List;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        String attachmentUrl,
        String status,
        ProjectShortResponse project,
        List<EmployeeShortResponse> assignees

) {
    public record ProjectShortResponse(
            String id,
            String title
    ) {}

    public record EmployeeShortResponse(
            String id,
            String userId,
            String firstName,
            String lastName,
            String departmentTitle
    ) {}
}
