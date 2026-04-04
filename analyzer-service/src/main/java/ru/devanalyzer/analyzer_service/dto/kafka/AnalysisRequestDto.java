package ru.devanalyzer.analyzer_service.dto.kafka;

import java.time.Instant;
import java.util.List;

public record AnalysisRequestDto(
        String requestId,
        String githubUsername,
        List<String> resumeTechStack,
        Instant timestamp
) {
}
