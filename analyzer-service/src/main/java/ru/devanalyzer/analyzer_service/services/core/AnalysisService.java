package ru.devanalyzer.analyzer_service.services.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.model.AnalysisStatus;
import ru.devanalyzer.analyzer_service.services.analysis.builder.AnalysisResultBuilder;
import ru.devanalyzer.analyzer_service.services.analysis.notification.AnalysisStatusNotifier;
import ru.devanalyzer.analyzer_service.services.github.AuthorshipVerificationService;
import ru.devanalyzer.analyzer_service.services.github.GitHubRepositoryService;
import ru.devanalyzer.analyzer_service.services.sonar.RepositoryScanService;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisStatusNotifier statusNotifier;
    private final AnalysisResultBuilder resultBuilder;
    private final RepositoryFilterService filterService;
    private final GitHubRepositoryService gitHubRepositoryService;
    private final AuthorshipVerificationService authorshipService;
    private final RepositoryScanService scanService;

    public void processAnalysisRequest(AnalysisRequestDto request) {
        log.info("Processing analysis for user: {}, userId: {}, requestId: {}",
                request.githubUsername(), request.userId(), request.requestId());

        try {

            List<GitHubRepository> allRepositories = fetchRepositories(request.githubUsername());

            statusNotifier.notifyStatus(request, AnalysisStatus.FILTERING);

            List<GitHubRepository> filteredRepositories = filterAndVerifyRepositories(
                    allRepositories,
                    request.githubUsername(),
                    request.languages()
            );

            statusNotifier.notifyStatus(request, AnalysisStatus.ANALYZING);

            List<RepositoryScanResult> scanResults = performCodeAnalysis(
                    filteredRepositories,
                    request.requestId()
            );

            List<String> requestedFilters = new ArrayList<>();
            if (request.languages() != null) {
                requestedFilters.addAll(request.languages());
            }
            if (request.techStack() != null) {
                requestedFilters.addAll(request.techStack());
            }

            AnalysisResult result = resultBuilder.build(
                    request.githubUsername(),
                    allRepositories.size(),
                    filteredRepositories,
                    scanResults,
                    requestedFilters.stream().distinct().sorted().toList()
            );

            statusNotifier.notifyCompleted(request, result);

        } catch (Exception e) {
            log.error("Error during analysis processing for user: {}, userId: {}",
                    request.githubUsername(), request.userId(), e);
            statusNotifier.notifyFailed(request, e);
        }
    }

    private List<GitHubRepository> fetchRepositories(String githubUsername) {
        List<GitHubRepository> repositories = gitHubRepositoryService.getUserRepositories(githubUsername);
        log.info("Fetched {} repositories for user: {}", repositories.size(), githubUsername);
        return repositories;
    }

    private List<GitHubRepository> filterAndVerifyRepositories(
            List<GitHubRepository> allRepositories,
            String githubUsername,
            List<String> languages
    ) {
        List<GitHubRepository> filtered = filterService.filterRepositories(allRepositories, languages)
                .stream()
                .filter(repo -> authorshipService.verifyOwnership(repo, githubUsername))
                .toList();

        log.info("After filtering: {} repositories remain for analysis", filtered.size());
        return filtered;
    }

    private List<RepositoryScanResult> performCodeAnalysis(
            List<GitHubRepository> repositories,
            String requestId
    ) {
        log.info("Starting SonarQube analysis for {} repositories", repositories.size());
        List<RepositoryScanResult> scanResults = scanService.scanRepositories(repositories, requestId);
        log.info("SonarQube analysis completed");
        return scanResults;
    }
}
