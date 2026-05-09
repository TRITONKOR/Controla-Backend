package org.tritonkor.controlabackend.user.dto;

import org.tritonkor.controlabackend.employee.entity.EmployeePositions;

import java.util.UUID;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        EmployeePositions position,
        UUID departmentId
) {}

