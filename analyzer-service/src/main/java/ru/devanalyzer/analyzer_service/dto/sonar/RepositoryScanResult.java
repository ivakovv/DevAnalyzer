package ru.devanalyzer.analyzer_service.dto.sonar;

public record RepositoryScanResult(
        String repositoryName,
        String language,
        SonarMetrics metrics,
        String status,
        String errorMessage
) {}
