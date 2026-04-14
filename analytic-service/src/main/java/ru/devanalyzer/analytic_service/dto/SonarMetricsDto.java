package ru.devanalyzer.analytic_service.dto;

import java.util.List;

public record SonarMetricsDto(
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
