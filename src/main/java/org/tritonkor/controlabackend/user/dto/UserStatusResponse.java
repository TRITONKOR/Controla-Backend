package org.tritonkor.controlabackend.user.dto;

import org.tritonkor.controlabackend.user.entity.Role;

import java.util.UUID;

public record UserStatusResponse(
        UUID userId,
        Role role,
        Boolean isApproved,
        Boolean isActive
) {
}

