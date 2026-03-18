package ru.devanalyzer.gateway_service.exception.error;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        int status,
        OffsetDateTime timestamp) {
}
