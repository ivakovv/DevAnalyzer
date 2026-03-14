package ru.devanalyzer.user_service.dto;

import lombok.Builder;
import ru.devanalyzer.user_service.enums.Role;

@Builder
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String patronymic,
        String lastName,
        Role role,
        String company,
        String position
) {
}
