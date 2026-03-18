package ru.devanalyzer.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank
        @Size(min=8,max = 30,message = "Пароль должен быть минимум 8 и максимум 30 символов")
        String password
) {
}
