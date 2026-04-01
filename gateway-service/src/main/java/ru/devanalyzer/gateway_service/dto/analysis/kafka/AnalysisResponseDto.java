package ru.devanalyzer.gateway_service.dto.analysis.kafka;

import java.time.OffsetDateTime;

public record AnalysisResponseDto(
        String requestId,
        String status,
        String websocketUrl,
        OffsetDateTime createdAt
) {
}
