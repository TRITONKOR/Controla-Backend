package org.tritonkor.controlabackend.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}

