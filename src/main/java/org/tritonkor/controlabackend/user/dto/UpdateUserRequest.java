package org.tritonkor.controlabackend.user.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        String avatar
) {}

