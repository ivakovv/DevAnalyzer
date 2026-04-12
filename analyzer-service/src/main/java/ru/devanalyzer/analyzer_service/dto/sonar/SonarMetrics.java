package ru.devanalyzer.analyzer_service.dto.sonar;

import java.util.List;

public record SonarMetrics(
        String qualityGateStatus,
        Integer bugs,
        Integer vulnerabilities,
        Integer codeSmells,
        Double coverage,
        Double duplications,
        Integer linesOfCode,
        String securityRating,
        String reliabilityRating,
        String maintainabilityRating,
        List<String> techStack
) {}
