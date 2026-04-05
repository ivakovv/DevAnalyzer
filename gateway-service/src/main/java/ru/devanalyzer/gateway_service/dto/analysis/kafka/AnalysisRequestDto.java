package ru.devanalyzer.gateway_service.dto.analysis.kafka;

import java.time.Instant;
import java.util.List;

public record AnalysisRequestDto(
        String requestId,
        Long userId,
        String githubUsername,
        List<String> languages,       
        List<String> techStack,    
        Instant timestamp
) {
}
