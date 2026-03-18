package ru.devanalyzer.user_service.dto;


public record UserUpdateRequest(

        String firstName,
        String patronymic,
        String lastName,
        String company,
        String position
) {
}
