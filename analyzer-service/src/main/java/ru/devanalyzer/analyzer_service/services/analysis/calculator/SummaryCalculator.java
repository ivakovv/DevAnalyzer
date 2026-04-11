package ru.devanalyzer.analyzer_service.services.analysis.calculator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.util.StatisticsCalculator;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class SummaryCalculator {

    public AnalysisSummary calculate(List<RepositoryScanResult> successfulResults) {
        return new AnalysisSummary(
                calculateTotalBugs(successfulResults),
                calculateTotalVulnerabilities(successfulResults),
                calculateTotalCodeSmells(successfulResults),
                calculateAverageCoverage(successfulResults),
                countPassedQualityGate(successfulResults),
                countFailedQualityGate(successfulResults),
                calculateMedianBugs(successfulResults),
                calculateMedianVulnerabilities(successfulResults),
                calculateMedianCodeSmells(successfulResults),
                calculateMedianCoverage(successfulResults),
                calculateMedianDuplications(successfulResults),
                calculateMedianLinesOfCode(successfulResults),
                calculateMedianSecurityRating(successfulResults),
                calculateMedianReliabilityRating(successfulResults),
                calculateMedianMaintainabilityRating(successfulResults)
        );
    }

    private int calculateTotalBugs(List<RepositoryScanResult> results) {
        return StatisticsCalculator.sum(results, r -> r.metrics().bugs());
    }

    private int calculateTotalVulnerabilities(List<RepositoryScanResult> results) {
        return StatisticsCalculator.sum(results, r -> r.metrics().vulnerabilities());
    }

    private int calculateTotalCodeSmells(List<RepositoryScanResult> results) {
        return StatisticsCalculator.sum(results, r -> r.metrics().codeSmells());
    }

    private double calculateAverageCoverage(List<RepositoryScanResult> results) {
        return StatisticsCalculator.round(
                StatisticsCalculator.average(results, r -> r.metrics().coverage()), 2);
    }

    private long countPassedQualityGate(List<RepositoryScanResult> results) {
        return results.stream()
                .filter(r -> "OK".equals(r.metrics().qualityGateStatus()))
                .count();
    }

    private long countFailedQualityGate(List<RepositoryScanResult> results) {
        return results.stream()
                .filter(r -> !"OK".equals(r.metrics().qualityGateStatus()))
                .count();
    }

    private double calculateMedianBugs(List<RepositoryScanResult> results) {
        return StatisticsCalculator.median(
                results.stream()
                        .map(r -> r.metrics().bugs() != null ? r.metrics().bugs() : 0)
                        .toList()
        );
    }

    private double calculateMedianVulnerabilities(List<RepositoryScanResult> results) {
        return StatisticsCalculator.median(
                results.stream()
                        .map(r -> r.metrics().vulnerabilities() != null ? r.metrics().vulnerabilities() : 0)
                        .toList()
        );
    }

    private double calculateMedianCodeSmells(List<RepositoryScanResult> results) {
        return StatisticsCalculator.median(
                results.stream()
                        .map(r -> r.metrics().codeSmells() != null ? r.metrics().codeSmells() : 0)
                        .toList()
        );
    }

    private double calculateMedianCoverage(List<RepositoryScanResult> results) {
        return StatisticsCalculator.medianDouble(
                results.stream()
                        .filter(r -> r.metrics().coverage() != null)
                        .map(r -> r.metrics().coverage())
                        .toList()
        );
    }

    private double calculateMedianDuplications(List<RepositoryScanResult> results) {
        return StatisticsCalculator.medianDouble(
                results.stream()
                        .filter(r -> r.metrics().duplications() != null)
                        .map(r -> r.metrics().duplications())
                        .toList()
        );
    }

    private double calculateMedianLinesOfCode(List<RepositoryScanResult> results) {
        return StatisticsCalculator.median(
                results.stream()
                        .map(r -> r.metrics().linesOfCode() != null ? r.metrics().linesOfCode() : 0)
                        .toList()
        );
    }

    private String calculateMedianSecurityRating(List<RepositoryScanResult> results) {
        return calculateMedianRating(results, r -> r.metrics().securityRating());
    }

    private String calculateMedianReliabilityRating(List<RepositoryScanResult> results) {
        return calculateMedianRating(results, r -> r.metrics().reliabilityRating());
    }

    private String calculateMedianMaintainabilityRating(List<RepositoryScanResult> results) {
        return calculateMedianRating(results, r -> r.metrics().maintainabilityRating());
    }

    private String calculateMedianRating(List<RepositoryScanResult> results,
                                        Function<RepositoryScanResult, String> ratingExtractor) {
        List<String> ratings = results.stream()
                .map(ratingExtractor)
                .filter(rating -> rating != null && !rating.isEmpty())
                .sorted()
                .toList();

        if (ratings.isEmpty()) {
            return null;
        }

        int middle = ratings.size() / 2;
        return ratings.size() % 2 == 0
                ? ratings.get(middle - 1)
                : ratings.get(middle);
    }
}
