package ru.devanalyzer.gateway_service.dto.analysis.kafka;

import java.time.OffsetDateTime;

public record AnalysisResponseDto(
        String requestId,
        Long userId,
        String status,
        String websocketUrl,
        OffsetDateTime createdAt
) {
}
