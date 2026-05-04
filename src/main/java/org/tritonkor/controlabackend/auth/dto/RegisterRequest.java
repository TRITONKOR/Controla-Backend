package org.tritonkor.controlabackend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Ім'я не може бути порожнім")
        String firstName,

        @NotBlank(message = "Прізвище не може бути порожнім")
        String lastName,

        @Email(message = "Невірний формат email")
        @NotBlank(message = "Email не може быть пустым")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        String password
) {
}

