package org.tritonkor.controlabackend.auth.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String accessToken,
        AuthUser user
) {

    public record AuthUser(
            java.util.UUID id,
            String email,
            String firstName,
            String lastName,
            String role,
            String avatar,
            Boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt

    ) {
    }
}

