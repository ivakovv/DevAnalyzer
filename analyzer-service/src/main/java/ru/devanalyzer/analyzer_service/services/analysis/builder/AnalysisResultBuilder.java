package ru.devanalyzer.analyzer_service.services.analysis.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.services.analysis.calculator.ScanResultCounter;
import ru.devanalyzer.analyzer_service.services.analysis.calculator.SummaryCalculator;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisResultBuilder {

    private final SummaryCalculator summaryCalculator;
    private final ScanResultCounter scanResultCounter;

    public AnalysisResult build(
            String githubUsername,
            int totalRepos,
            List<GitHubRepository> filteredRepositories,
            List<RepositoryScanResult> scanResults,
            List<String> requestedFilters
    ) {
        List<RepositoryScanResult> successfulResults = scanResultCounter.extractSuccessfulResults(scanResults);

        return new AnalysisResult(
                githubUsername,
                totalRepos,
                filteredRepositories.size(),
                filteredRepositories.size(),
                scanResultCounter.countSuccessful(scanResults),
                scanResultCounter.countFailed(scanResults),
                buildSummary(successfulResults),
                buildFoundTechStack(successfulResults),
                requestedFilters != null ? requestedFilters : List.of(),
                scanResults,
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

    private String formatCompletionMessage(List<RepositoryScanResult> scanResults) {
        long successful = scanResultCounter.countSuccessful(scanResults);
        long failed = scanResultCounter.countFailed(scanResults);

        return String.format(
                "Analysis completed. %d repositories scanned successfully, %d failed.",
                successful, failed
        );
    }
}
