package ru.devanalyzer.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min=8,max = 30,message = "Пароль должен быть минимум 8 и максимум 30 символов")
        String password,

        @NotBlank(message = "Имя обязательно")
        @Size(min = 2,message = "Имя должно состоять минимум из 2 букв")
        String firstName,

        String patronymic,

        @Size(min = 2,message = "Фамилия должна состоять минимум из 2 букв")
        @NotBlank(message = "Фамилия обязательна")
        String lastName,

        String company,

        String position
) {
}
