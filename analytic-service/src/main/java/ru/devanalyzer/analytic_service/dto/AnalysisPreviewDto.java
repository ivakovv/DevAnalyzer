package ru.devanalyzer.analytic_service.dto;

import java.time.Instant;

public record AnalysisPreviewDto(
        String requestId,
        String githubUsername,
        Integer overallScore,
        int totalRepositories,
        int verifiedRepositories,
        long successfulScans,
        Instant createdAt
) {}
