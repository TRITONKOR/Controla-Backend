package org.tritonkor.controlabackend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
