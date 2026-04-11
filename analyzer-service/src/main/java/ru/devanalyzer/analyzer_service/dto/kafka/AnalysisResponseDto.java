package ru.devanalyzer.analyzer_service.dto.kafka;

import java.time.Instant;

public record AnalysisResponseDto(
        String requestId,
        Long userId,
        String status,
        Instant processedAt
) {
}
