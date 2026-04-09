package ru.devanalyzer.analyzer_service.dto;


import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;

import java.util.List;

public record AnalysisResult(
        String githubUsername,
        int totalRepositories,
        int filteredRepositories,
        int verifiedRepositories,
        long successfulScans,
        long failedScans,
        AnalysisSummary summary,
        List<String> foundTechStack,
        List<String> requestedFilters,
        List<RepositoryScanResult> repositories,
        String message
) {}
