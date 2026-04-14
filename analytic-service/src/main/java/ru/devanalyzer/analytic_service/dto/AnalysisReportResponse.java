package ru.devanalyzer.analytic_service.dto;

import java.time.Instant;
import java.util.List;

public record AnalysisReportResponse(
        String requestId,
        Long userId,
        String githubUsername,
        int totalRepositories,
        int filteredRepositories,
        int verifiedRepositories,
        long successfulScans,
        long failedScans,
        Integer overallScore,
        AnalysisSummaryDto summary,
        TechStackAnalysisDto techStackAnalysis,
        List<RepositoryScanResultDto> repositories,
        GitHubStatsDto gitHubStats,
        List<GitHubRepoDto> gitHubRepo,
        Instant createdAt
) {}
