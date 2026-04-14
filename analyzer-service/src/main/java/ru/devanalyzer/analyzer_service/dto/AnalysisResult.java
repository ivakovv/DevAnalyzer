package ru.devanalyzer.analyzer_service.dto;


import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;

import java.util.List;

public record AnalysisResult(
        String requestId,
        Long userId,
        String githubUsername,
        int totalRepositories,
        int filteredRepositories,
        int verifiedRepositories,
        long successfulScans,
        long failedScans,
        AnalysisSummary summary,
        TechStackAnalysis techStackAnalysis,
        List<RepositoryScanResult> repositories,
        Integer overallScore,
        GitHubStats gitHubStats,
        List<GitHubRepo> gitHubRepo,
        String message
) {}
