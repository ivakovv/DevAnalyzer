package ru.devanalyzer.analyzer_service.dto;

public record AnalysisSummary(
        int totalBugs,
        int totalVulnerabilities,
        int totalCodeSmells,
        double averageCoverage,
        long passedQualityGate,
        long failedQualityGate,
        double medianBugs,
        double medianVulnerabilities,
        double medianCodeSmells,
        double medianCoverage,
        double medianDuplications,
        double medianLinesOfCode,
        String medianSecurityRating,
        String medianReliabilityRating,
        String medianMaintainabilityRating
) {
}
