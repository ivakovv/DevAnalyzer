package ru.devanalyzer.analytic_service.dto;

import java.util.List;

public record AnalysisResultDto(
        String requestId,
        Long userId,
        String githubUsername,
        int totalRepositories,
        int filteredRepositories,
        int verifiedRepositories,
        long successfulScans,
        long failedScans,
        AnalysisSummaryDto summary,
        TechStackAnalysisDto techStackAnalysis,
        List<RepositoryScanResultDto> repositories,
        Integer overallScore,
        GitHubStatsDto gitHubStats,
        List<GitHubRepoDto> gitHubRepo,
        String message
) {}
