package org.tritonkor.controlabackend.user.dto;

import org.tritonkor.controlabackend.user.entity.Role;

public record UserResponse(
        String email,
        Role role,
        Boolean isApproved,
        Boolean isActive,
        String avatar
) {
}
