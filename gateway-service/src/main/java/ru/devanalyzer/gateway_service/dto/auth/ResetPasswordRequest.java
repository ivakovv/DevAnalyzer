package ru.devanalyzer.gateway_service.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank(message = "Токен обязателен")
        String token,
        
        @NotBlank(message = "Новый пароль обязателен")
        String newPassword
) {
}
