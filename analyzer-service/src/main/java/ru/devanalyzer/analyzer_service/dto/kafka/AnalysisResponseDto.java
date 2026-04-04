package ru.devanalyzer.analyzer_service.dto.kafka;

import java.time.Instant;

public record AnalysisResponseDto(
        String requestId,
        String status,
        //TODO убрать object и сделать кастомный класс
        Object result,
        Instant processedAt
) {
}
