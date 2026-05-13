package org.tritonkor.controlabackend.employee.dto;

import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String firstName,
        String lastName,
        String avatar,
        UUID departmentId,
        String departmentTitle,
        Long projectsCount,
        Double productivity,
        Long assignedTasksLastMonth,
        Long completedTasksLastMonth
) {
}
