package ru.devanalyzer.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ru.devanalyzer.user_service.enums.Role;

public record UserCreateRequest(

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        String password,

        @NotBlank(message = "Имя обязательно")
        String firstName,

        String patronymic,

        @NotBlank(message = "Фамилия обязательна")
        String lastName,

        Role role,

        String company,

        String position
) {
}
