package ru.devanalyzer.gateway_service.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Токен обязателен")
        String token,

        @Size(min = 6, message = "Минимальная длина пароля 6 символов")
        @NotBlank(message = "Новый пароль обязателен")
        String newPassword
) {
}
