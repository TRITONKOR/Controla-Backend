package org.tritonkor.controlabackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "Невірний формат email")
        @NotBlank(message = "Email не може бути порожнім")
        String email,

        @NotBlank(message = "Пароль не може бути порожнім")
        String password
) {
}
