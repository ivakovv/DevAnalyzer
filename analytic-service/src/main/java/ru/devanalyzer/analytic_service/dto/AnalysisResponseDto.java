package ru.devanalyzer.analytic_service.dto;

import java.time.Instant;

public record AnalysisResponseDto(
        String requestId,
        Long userId,
        String status,
        Instant processedAt
) {}
