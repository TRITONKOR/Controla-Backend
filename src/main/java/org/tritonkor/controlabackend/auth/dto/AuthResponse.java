package org.tritonkor.controlabackend.auth.dto;

public record AuthResponse(
        String accessToken,
        AuthUser user
) {

    public record AuthUser(
            java.util.UUID id,
            String email,
            String firstName,
            String lastName
    ) {
    }
}

