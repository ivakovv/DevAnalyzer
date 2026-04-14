package ru.devanalyzer.analytic_service.dto;

public record RepositoryScanResultDto(
        String repositoryName,
        String language,
        SonarMetricsDto metrics,
        String status,
        String errorMessage
) {}
