package org.tritonkor.controlabackend.user.dto;

import java.util.UUID;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        UUID departmentId
) {}

