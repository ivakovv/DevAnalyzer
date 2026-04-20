package ru.devanalyzer.analyzer_service.services.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.messaging.AnalysisResultProducer;
import ru.devanalyzer.analyzer_service.model.AnalysisStatus;
import ru.devanalyzer.analyzer_service.services.analysis.builder.AnalysisResultBuilder;
import ru.devanalyzer.analyzer_service.services.analysis.notification.AnalysisStatusNotifier;
import ru.devanalyzer.analyzer_service.services.github.AuthorshipVerificationService;
import ru.devanalyzer.analyzer_service.services.github.GitHubRepositoryService;
import ru.devanalyzer.analyzer_service.services.sonar.RepositoryScanService;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AnalysisStatusNotifier statusNotifier;

    @Mock
    private AnalysisResultBuilder resultBuilder;

    @Mock
    private RepositoryFilterService filterService;

    @Mock
    private GitHubRepositoryService gitHubRepositoryService;

    @Mock
    private AuthorshipVerificationService authorshipService;

    @Mock
    private RepositoryScanService scanService;

    @Mock
    private AnalysisResultProducer resultProducer;

    @InjectMocks
    private AnalysisService analysisService;

    @Captor
    private ArgumentCaptor<AnalysisResult> resultCaptor;

    @Captor
    private ArgumentCaptor<List<String>> requestedFiltersCaptor;

    private AnalysisRequestDto request;
    private List<GitHubRepository> allRepositories;
    private List<GitHubRepository> filteredRepositories;
    private List<RepositoryScanResult> scanResults;
    private AnalysisResult analysisResult;

    @BeforeEach
    void setUp() {
        request = new AnalysisRequestDto(
                "request-123",
                1L,
                "testuser",
                List.of("Java", "Python"),
                List.of("Spring", "Django"),
                Instant.now()
        );

        allRepositories = List.of(
                createRepository("repo1", "Java"),
                createRepository("repo2", "Python"),
                createRepository("repo3", "JavaScript")
        );

        filteredRepositories = List.of(
                createRepository("repo1", "Java"),
                createRepository("repo2", "Python")
        );

        scanResults = List.of(
                createScanResult("repo1", "SUCCESS"),
                createScanResult("repo2", "SUCCESS")
        );

        analysisResult = new AnalysisResult(
                "request-123", 1L, "testuser", 3, 2, 2, 2, 0,
                null, null, scanResults, 85, null, null, "Analysis completed"
        );
    }

    @Test
    void processAnalysisRequest_shouldCompleteSuccessfully() {
        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(allRepositories);
        when(filterService.filterRepositories(allRepositories, request.languages()))
                .thenReturn(filteredRepositories);
        when(authorshipService.verifyOwnership(any(), eq("testuser")))
                .thenReturn(true);
        when(scanService.scanRepositories(filteredRepositories, "request-123"))
                .thenReturn(scanResults);
        when(resultBuilder.build(
                eq("request-123"),
                eq(1L),
                eq("testuser"),
                eq(3),
                eq(filteredRepositories),
                eq(scanResults),
                anyList()
        )).thenReturn(analysisResult);

        analysisService.processAnalysisRequest(request);

        verify(statusNotifier).notifyStatus(request, AnalysisStatus.FILTERING);
        verify(statusNotifier).notifyStatus(request, AnalysisStatus.ANALYZING);
        verify(statusNotifier).notifyStatus(request, AnalysisStatus.BUILDING_REPORT);
        verify(resultProducer).send("request-123", analysisResult);
        verify(statusNotifier, never()).notifyFailed(any(), any());
    }

    @Test
    void processAnalysisRequest_shouldHandleFetchError() {
        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenThrow(new RuntimeException("GitHub API error"));

        analysisService.processAnalysisRequest(request);

        verify(statusNotifier).notifyFailed(eq(request), any(RuntimeException.class));
        verify(resultProducer, never()).send(any(), any());
    }

    @Test
    void processAnalysisRequest_shouldHandleFilterError() {
        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(allRepositories);
        when(filterService.filterRepositories(any(), any()))
                .thenThrow(new RuntimeException("Filter error"));

        analysisService.processAnalysisRequest(request);

        verify(statusNotifier).notifyFailed(eq(request), any(RuntimeException.class));
        verify(resultProducer, never()).send(any(), any());
    }

    @Test
    void processAnalysisRequest_shouldHandleScanError() {
        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(allRepositories);
        when(filterService.filterRepositories(allRepositories, request.languages()))
                .thenReturn(filteredRepositories);
        when(authorshipService.verifyOwnership(any(), eq("testuser")))
                .thenReturn(true);
        when(scanService.scanRepositories(filteredRepositories, "request-123"))
                .thenThrow(new RuntimeException("Scan error"));

        analysisService.processAnalysisRequest(request);

        verify(statusNotifier).notifyFailed(eq(request), any(RuntimeException.class));
        verify(resultProducer, never()).send(any(), any());
    }

    @Test
    void processAnalysisRequest_shouldHandleBuildError() {
        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(allRepositories);
        when(filterService.filterRepositories(allRepositories, request.languages()))
                .thenReturn(filteredRepositories);
        when(authorshipService.verifyOwnership(any(), eq("testuser")))
                .thenReturn(true);
        when(scanService.scanRepositories(filteredRepositories, "request-123"))
                .thenReturn(scanResults);
        when(resultBuilder.build(any(), any(), any(), anyInt(), any(), any(), any()))
                .thenThrow(new RuntimeException("Build error"));

        analysisService.processAnalysisRequest(request);

        verify(statusNotifier).notifyFailed(eq(request), any(RuntimeException.class));
        verify(resultProducer, never()).send(any(), any());
    }

    @Test
    void processAnalysisRequest_shouldCombineLanguagesAndTechStack() {
        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(allRepositories);
        when(filterService.filterRepositories(allRepositories, request.languages()))
                .thenReturn(filteredRepositories);
        when(authorshipService.verifyOwnership(any(), eq("testuser")))
                .thenReturn(true);
        when(scanService.scanRepositories(filteredRepositories, "request-123"))
                .thenReturn(scanResults);
        when(resultBuilder.build(
                eq("request-123"),
                eq(1L),
                eq("testuser"),
                eq(3),
                eq(filteredRepositories),
                eq(scanResults),
                anyList()
        )).thenReturn(analysisResult);

        analysisService.processAnalysisRequest(request);

        verify(resultBuilder).build(
                eq("request-123"),
                eq(1L),
                eq("testuser"),
                eq(3),
                eq(filteredRepositories),
                eq(scanResults),
                requestedFiltersCaptor.capture()
        );

        List<String> requestedFilters = requestedFiltersCaptor.getValue();
        assertThat(requestedFilters).containsExactlyInAnyOrder("Django", "Java", "Python", "Spring");
    }

    @Test
    void processAnalysisRequest_shouldHandleNullLanguagesAndTechStack() {
        AnalysisRequestDto requestWithNulls = new AnalysisRequestDto(
                "request-123", 1L, "testuser", null, null, Instant.now()
        );

        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(allRepositories);
        when(filterService.filterRepositories(allRepositories, null))
                .thenReturn(filteredRepositories);
        when(authorshipService.verifyOwnership(any(), eq("testuser")))
                .thenReturn(true);
        when(scanService.scanRepositories(filteredRepositories, "request-123"))
                .thenReturn(scanResults);
        when(resultBuilder.build(
                eq("request-123"),
                eq(1L),
                eq("testuser"),
                eq(3),
                eq(filteredRepositories),
                eq(scanResults),
                eq(List.of())
        )).thenReturn(analysisResult);

        analysisService.processAnalysisRequest(requestWithNulls);

        verify(resultBuilder).build(
                eq("request-123"),
                eq(1L),
                eq("testuser"),
                eq(3),
                eq(filteredRepositories),
                eq(scanResults),
                eq(List.of())
        );
    }

    @Test
    void processAnalysisRequest_shouldFilterByAuthorship() {
        GitHubRepository ownedRepo = createRepository("owned", "Java");
        GitHubRepository notOwnedRepo = createRepository("not-owned", "Python");

        when(gitHubRepositoryService.getUserRepositories("testuser"))
                .thenReturn(List.of(ownedRepo, notOwnedRepo));
        when(filterService.filterRepositories(any(), any()))
                .thenReturn(List.of(ownedRepo, notOwnedRepo));
        when(authorshipService.verifyOwnership(ownedRepo, "testuser"))
                .thenReturn(true);
        when(authorshipService.verifyOwnership(notOwnedRepo, "testuser"))
                .thenReturn(false);
        when(scanService.scanRepositories(eq(List.of(ownedRepo)), anyString()))
                .thenReturn(scanResults);
        when(resultBuilder.build(any(), any(), any(), anyInt(), any(), any(), any()))
                .thenReturn(analysisResult);

        analysisService.processAnalysisRequest(request);

        verify(scanService).scanRepositories(List.of(ownedRepo), "request-123");
    }

    private GitHubRepository createRepository(String name, String language) {
        return new GitHubRepository(
                name,
                "user/" + name,
                "https://github.com/user/" + name + ".git",
                new GitHubRepository.Owner("user"),
                false,
                1024,
                10,
                5,
                language,
                List.of(language),
                Instant.now(),
                Instant.now().minus(365, java.time.temporal.ChronoUnit.DAYS),
                "Test repo",
                true,
                3,
                "main",
                100,
                List.of()
        );
    }

    private RepositoryScanResult createScanResult(String repoName, String status) {
        return new RepositoryScanResult(
                repoName,
                "Java",
                new SonarMetrics(
                        "OK", 5, 1, 10, 85.5, 3.2, 1000,
                        "A", "B", "A", List.of("Java")
                ),
                status,
                null
        );
    }
}
