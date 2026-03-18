package ru.devanalyzer.gateway_service.dto.user;

public record UserValidationResponseDto(Long userId, String email, String role) {
}
