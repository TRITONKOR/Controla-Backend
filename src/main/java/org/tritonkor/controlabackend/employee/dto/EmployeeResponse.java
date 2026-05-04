package org.tritonkor.controlabackend.employee.dto;

import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String firstName,
        String lastName,
        String position,
        String avatar,
        UUID departmentId,
        String departmentTitle,
        int projectsCount,
        int tasksCount,
        int doneTasksCount
) {
}
