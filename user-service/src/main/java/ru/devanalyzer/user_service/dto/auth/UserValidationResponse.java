package ru.devanalyzer.user_service.dto.auth;

public record UserValidationResponse(Long userId, String email, String role) {
}
