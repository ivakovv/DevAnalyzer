package ru.devanalyzer.analyzer_service.services.analysis.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;
import ru.devanalyzer.analyzer_service.dto.TechStackAnalysis;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.services.analysis.calculator.ScanResultCounter;
import ru.devanalyzer.analyzer_service.services.analysis.calculator.SummaryCalculator;
import ru.devanalyzer.analyzer_service.util.OverallScoreCalculator;
import ru.devanalyzer.analyzer_service.util.StatisticsCalculator;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalysisResultBuilder {

    private final SummaryCalculator summaryCalculator;
    private final ScanResultCounter scanResultCounter;
    private final OverallScoreCalculator scoreCalculator;

    public AnalysisResult build(
            String githubUsername,
            int totalRepos,
            List<GitHubRepository> filteredRepositories,
            List<RepositoryScanResult> scanResults,
            List<String> requestedFilters
    ) {
        List<RepositoryScanResult> successfulResults = scanResultCounter.extractSuccessfulResults(scanResults);
        AnalysisSummary summary = buildSummary(successfulResults);
        Integer overallScore = scoreCalculator.calculateScore(summary);

        return new AnalysisResult(
                githubUsername,
                totalRepos,
                filteredRepositories.size(),
                filteredRepositories.size(),
                scanResultCounter.countSuccessful(scanResults),
                scanResultCounter.countFailed(scanResults),
                summary,
                buildTechStackAnalysis(successfulResults, requestedFilters),
                scanResults,
                overallScore,
                formatCompletionMessage(scanResults)
        );
    }

    private AnalysisSummary buildSummary(List<RepositoryScanResult> successfulResults) {
        return summaryCalculator.calculate(successfulResults);
    }

    private List<String> buildFoundTechStack(List<RepositoryScanResult> successfulResults) {
        return successfulResults.stream()
                .flatMap(result -> result.metrics().techStack().stream())
                .distinct()
                .sorted()
                .toList();
    }

    private TechStackAnalysis buildTechStackAnalysis (List<RepositoryScanResult> successfulResults, List<String> requestedFilters) {
        List<String> foundTechStack = buildFoundTechStack(successfulResults);
        List<String> notFoundTechStack = requestedFilters.stream()
                .filter(element -> !foundTechStack.contains(element))
                .collect(Collectors.toList());

        return new TechStackAnalysis(
                requestedFilters,
                foundTechStack,
                notFoundTechStack,
                (100 - StatisticsCalculator.calculatePercentage(notFoundTechStack.size(), requestedFilters.size()))
        );
    }

    private String formatCompletionMessage(List<RepositoryScanResult> scanResults) {
        long successful = scanResultCounter.countSuccessful(scanResults);
        long failed = scanResultCounter.countFailed(scanResults);

        return String.format(
                "Analysis completed. %d repositories scanned successfully, %d failed.",
                successful, failed
        );
    }
}
