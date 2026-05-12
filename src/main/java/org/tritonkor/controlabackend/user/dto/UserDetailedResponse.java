package org.tritonkor.controlabackend.user.dto;

import org.tritonkor.controlabackend.user.entity.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailedResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Boolean isApproved,
        Boolean isActive,
        String avatar,
        LocalDateTime createdAt
) {
}
