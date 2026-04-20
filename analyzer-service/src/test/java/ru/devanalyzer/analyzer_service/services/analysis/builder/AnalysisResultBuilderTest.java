package ru.devanalyzer.analyzer_service.services.analysis.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;
import ru.devanalyzer.analyzer_service.dto.GitHubRepo;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.dto.TechStackAnalysis;
import ru.devanalyzer.analyzer_service.dto.WeekActivity;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.services.GitHubService;
import ru.devanalyzer.analyzer_service.services.analysis.calculator.ScanResultCounter;
import ru.devanalyzer.analyzer_service.services.analysis.calculator.SummaryCalculator;
import ru.devanalyzer.analyzer_service.util.OverallScoreCalculator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisResultBuilderTest {

    @Mock
    private SummaryCalculator summaryCalculator;

    @Mock
    private ScanResultCounter scanResultCounter;

    @Mock
    private OverallScoreCalculator scoreCalculator;

    @Mock
    private GitHubService gitHubService;

    @InjectMocks
    private AnalysisResultBuilder builder;

    private List<GitHubRepository> filteredRepositories;
    private List<RepositoryScanResult> scanResults;
    private List<RepositoryScanResult> successfulResults;
    private AnalysisSummary summary;
    private GitHubStats gitHubStats;
    private List<GitHubRepo> gitHubRepos;

    @BeforeEach
    void setUp() {
        filteredRepositories = List.of(
                createRepository("repo1", "Java"),
                createRepository("repo2", "Python")
        );

        successfulResults = List.of(
                createScanResult("repo1", "SUCCESS", "OK", 5, 1, 10, List.of("Java", "Spring")),
                createScanResult("repo2", "SUCCESS", "OK", 3, 0, 5, List.of("Python", "Django"))
        );

        scanResults = List.of(
                successfulResults.get(0),
                successfulResults.get(1),
                createScanResult("repo3", "FAILED", null, 0, 0, 0, List.of())
        );

        summary = new AnalysisSummary(
                8, 1, 15, 85.5, 2, 0,
                5, 1, 10, 85.5, 3.2, 5000,
                "A", "B", "A"
        );

        gitHubStats = new GitHubStats(
                12345L, "testuser", "Test User", "Moscow", "Company",
                10, 100, 20, 50, 500, 1000L,
                List.of(new WeekActivity(LocalDate.now(), new int[]{1, 2, 3}, 6))
        );

        gitHubRepos = List.of(
                new GitHubRepo("repo1", "Desc1", "url1", 10, 5),
                new GitHubRepo("repo2", "Desc2", "url2", 20, 8)
        );
    }

    @Test
    void build_shouldCreateCompleteAnalysisResult() {
        when(scanResultCounter.extractSuccessfulResults(scanResults))
                .thenReturn(successfulResults);
        when(scanResultCounter.countSuccessful(scanResults)).thenReturn(2L);
        when(scanResultCounter.countFailed(scanResults)).thenReturn(1L);
        when(summaryCalculator.calculate(successfulResults)).thenReturn(summary);
        when(scoreCalculator.calculateScore(summary)).thenReturn(85);
        when(gitHubService.getStats("testuser")).thenReturn(gitHubStats);
        when(gitHubService.getRepositories("testuser")).thenReturn(gitHubRepos);

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                5,
                filteredRepositories,
                scanResults,
                List.of("Java", "Spring")
        );

        assertThat(result.requestId()).isEqualTo("request-123");
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.githubUsername()).isEqualTo("testuser");
        assertThat(result.totalRepositories()).isEqualTo(5);
        assertThat(result.filteredRepositories()).isEqualTo(2);
        assertThat(result.verifiedRepositories()).isEqualTo(2);
        assertThat(result.successfulScans()).isEqualTo(2);
        assertThat(result.failedScans()).isEqualTo(1);
        assertThat(result.summary()).isEqualTo(summary);
        assertThat(result.overallScore()).isEqualTo(85);
        assertThat(result.gitHubStats()).isEqualTo(gitHubStats);
        assertThat(result.gitHubRepo()).isEqualTo(gitHubRepos);
        assertThat(result.message()).contains("2 repositories scanned successfully", "1 failed");
    }

    @Test
    void build_shouldCalculateTechStackAnalysisCorrectly() {
        when(scanResultCounter.extractSuccessfulResults(scanResults))
                .thenReturn(successfulResults);
        when(scanResultCounter.countSuccessful(scanResults)).thenReturn(2L);
        when(scanResultCounter.countFailed(scanResults)).thenReturn(1L);
        when(summaryCalculator.calculate(any())).thenReturn(summary);
        when(scoreCalculator.calculateScore(any())).thenReturn(85);
        when(gitHubService.getStats(any())).thenReturn(gitHubStats);
        when(gitHubService.getRepositories(any())).thenReturn(gitHubRepos);

        List<String> requestedFilters = List.of("Java", "Spring", "React", "Django");

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                5,
                filteredRepositories,
                scanResults,
                requestedFilters
        );

        TechStackAnalysis techStack = result.techStackAnalysis();
        assertThat(techStack.requestedFilters()).containsExactlyElementsOf(requestedFilters);
        assertThat(techStack.foundTechStack()).containsExactlyInAnyOrder("Django", "Java", "Python", "Spring");
        assertThat(techStack.notFoundTechStack()).containsExactly("React");
        assertThat(techStack.percentageFound()).isEqualTo(75); // 3 из 4 найдено = 75%
    }

    @Test
    void build_shouldHandleEmptyScanResults() {
        when(scanResultCounter.extractSuccessfulResults(anyList())).thenReturn(List.of());
        when(scanResultCounter.countSuccessful(anyList())).thenReturn(0L);
        when(scanResultCounter.countFailed(anyList())).thenReturn(0L);
        when(summaryCalculator.calculate(anyList())).thenReturn(summary);
        when(scoreCalculator.calculateScore(any())).thenReturn(0);
        when(gitHubService.getStats(any())).thenReturn(gitHubStats);
        when(gitHubService.getRepositories(any())).thenReturn(gitHubRepos);

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                0,
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(result.successfulScans()).isZero();
        assertThat(result.failedScans()).isZero();
        assertThat(result.techStackAnalysis().foundTechStack()).isEmpty();
        assertThat(result.techStackAnalysis().percentageFound()).isEqualTo(100); // 0 из 0 = 100%
    }

    @Test
    void build_shouldHandleAllFailedScans() {
        List<RepositoryScanResult> allFailed = List.of(
                createScanResult("repo1", "FAILED", null, 0, 0, 0, List.of()),
                createScanResult("repo2", "FAILED", null, 0, 0, 0, List.of())
        );

        when(scanResultCounter.extractSuccessfulResults(allFailed)).thenReturn(List.of());
        when(scanResultCounter.countSuccessful(allFailed)).thenReturn(0L);
        when(scanResultCounter.countFailed(allFailed)).thenReturn(2L);
        when(summaryCalculator.calculate(anyList())).thenReturn(summary);
        when(scoreCalculator.calculateScore(any())).thenReturn(0);
        when(gitHubService.getStats(any())).thenReturn(gitHubStats);
        when(gitHubService.getRepositories(any())).thenReturn(gitHubRepos);

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                2,
                filteredRepositories,
                allFailed,
                List.of("Java")
        );

        assertThat(result.successfulScans()).isZero();
        assertThat(result.failedScans()).isEqualTo(2);
        assertThat(result.message()).contains("0 repositories scanned successfully", "2 failed");
    }

    @Test
    void build_shouldDeduplicateFoundTechStack() {
        List<RepositoryScanResult> resultsWithDuplicates = List.of(
                createScanResult("repo1", "SUCCESS", "OK", 5, 1, 10, List.of("Java", "Spring", "Java")),
                createScanResult("repo2", "SUCCESS", "OK", 3, 0, 5, List.of("Java", "Spring", "Spring"))
        );

        when(scanResultCounter.extractSuccessfulResults(anyList())).thenReturn(resultsWithDuplicates);
        when(scanResultCounter.countSuccessful(anyList())).thenReturn(2L);
        when(scanResultCounter.countFailed(anyList())).thenReturn(0L);
        when(summaryCalculator.calculate(any())).thenReturn(summary);
        when(scoreCalculator.calculateScore(any())).thenReturn(85);
        when(gitHubService.getStats(any())).thenReturn(gitHubStats);
        when(gitHubService.getRepositories(any())).thenReturn(gitHubRepos);

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                2,
                filteredRepositories,
                resultsWithDuplicates,
                List.of("Java", "Spring")
        );

        assertThat(result.techStackAnalysis().foundTechStack())
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    void build_shouldCalculatePercentageFound_whenAllRequestedFound() {
        when(scanResultCounter.extractSuccessfulResults(anyList())).thenReturn(successfulResults);
        when(scanResultCounter.countSuccessful(anyList())).thenReturn(2L);
        when(scanResultCounter.countFailed(anyList())).thenReturn(0L);
        when(summaryCalculator.calculate(any())).thenReturn(summary);
        when(scoreCalculator.calculateScore(any())).thenReturn(85);
        when(gitHubService.getStats(any())).thenReturn(gitHubStats);
        when(gitHubService.getRepositories(any())).thenReturn(gitHubRepos);

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                2,
                filteredRepositories,
                scanResults,
                List.of("Java", "Python")
        );

        assertThat(result.techStackAnalysis().notFoundTechStack()).isEmpty();
        assertThat(result.techStackAnalysis().percentageFound()).isEqualTo(100);
    }

    @Test
    void build_shouldCalculatePercentageFound_whenNoneRequestedFound() {
        when(scanResultCounter.extractSuccessfulResults(anyList())).thenReturn(successfulResults);
        when(scanResultCounter.countSuccessful(anyList())).thenReturn(2L);
        when(scanResultCounter.countFailed(anyList())).thenReturn(0L);
        when(summaryCalculator.calculate(any())).thenReturn(summary);
        when(scoreCalculator.calculateScore(any())).thenReturn(85);
        when(gitHubService.getStats(any())).thenReturn(gitHubStats);
        when(gitHubService.getRepositories(any())).thenReturn(gitHubRepos);

        AnalysisResult result = builder.build(
                "request-123",
                1L,
                "testuser",
                2,
                filteredRepositories,
                scanResults,
                List.of("Ruby", "Go")
        );

        assertThat(result.techStackAnalysis().notFoundTechStack())
                .containsExactlyInAnyOrder("Go", "Ruby");
        assertThat(result.techStackAnalysis().percentageFound()).isZero();
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

    private RepositoryScanResult createScanResult(String name, String status, String qualityGate,
                                                  int bugs, int vulnerabilities, int codeSmells,
                                                  List<String> techStack) {
        SonarMetrics metrics = null;
        if ("SUCCESS".equals(status)) {
            metrics = new SonarMetrics(
                    qualityGate, bugs, vulnerabilities, codeSmells,
                    85.5, 3.2, 5000,
                    "A", "B", "A", techStack
            );
        }
        return new RepositoryScanResult(
                name, "Java", metrics, status,
                "SUCCESS".equals(status) ? null : "Scan failed"
        );
    }
}
