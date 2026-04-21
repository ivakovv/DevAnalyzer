package ru.devanalyzer.analyzer_service.services.sonar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.clients.SonarQubeClient;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.services.cache.SonarMetricsCacheService;
import ru.devanalyzer.analyzer_service.services.detection.FrameworkDetector;
import ru.devanalyzer.analyzer_service.services.git.GitRepositoryCloner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SonarQubeServiceTest {

    @Mock
    private SonarMetricsCacheService cacheService;

    @Mock
    private GitRepositoryCloner cloner;

    @Mock
    private SonarScannerExecutor scanner;

    @Mock
    private SonarQubeClient client;

    @Mock
    private FrameworkDetector frameworkDetector;

    @InjectMocks
    private SonarQubeService sonarQubeService;

    private GitHubRepository repository;
    private SonarMetrics cachedMetrics;
    private SonarMetrics scannedMetrics;

    @BeforeEach
    void setUp() {
        repository = new GitHubRepository(
                "test-repo",
                "testuser/test-repo",
                "https://github.com/testuser/test-repo.git",
                new GitHubRepository.Owner("testuser"),
                false,
                1024,
                10,
                5,
                "Java",
                List.of("Java", "Kotlin"),
                Instant.now(),
                Instant.now().minus(365, java.time.temporal.ChronoUnit.DAYS),
                "Test repository",
                true,
                3,
                "main",
                100,
                List.of("spring-boot", "docker")
        );

        cachedMetrics = new SonarMetrics(
                "OK", 5, 1, 10, 85.5, 3.2, 1000,
                "A", "B", "A", List.of("Java", "Kotlin")
        );

        scannedMetrics = new SonarMetrics(
                null, 5, 1, 10, 85.5, 3.2, 1000,
                "A", "B", "A", List.of("Java", "Kotlin")
        );
    }

    @Test
    void analyzeRepository_shouldReturnCachedMetrics_whenCacheHit() {
        when(cacheService.findCached(repository))
                .thenReturn(Optional.of(cachedMetrics));

        SonarMetrics result = sonarQubeService.analyzeRepository(repository, "scan-123", "/tmp/repo");

        assertThat(result).isEqualTo(cachedMetrics);
        verify(cloner, never()).cloneRepository(anyString(), anyString());
        verify(scanner, never()).executeScan(anyString(), anyString(), anyString());
    }

    @Test
    void analyzeRepository_shouldPerformFullAnalysis_whenCacheMiss() {
        when(cacheService.findCached(repository)).thenReturn(Optional.empty());
        when(client.getProjectMetrics(anyString())).thenReturn(scannedMetrics);
        when(client.getQualityGateStatus(anyString())).thenReturn("OK");
        when(frameworkDetector.detectFrameworks(any(), anyString()))
                .thenReturn(List.of("Spring Boot", "Docker"));

        SonarMetrics result = sonarQubeService.analyzeRepository(repository, "scan-123", "/tmp/repo");

        verify(cloner).cloneRepository(repository.cloneUrl(), "/tmp/repo");
        verify(scanner).executeScan(anyString(), eq("test-repo"), eq("/tmp/repo"));
        verify(client).getProjectMetrics(anyString());
        verify(client).getQualityGateStatus(anyString());
        verify(cacheService).save(eq(repository), any());

        assertThat(result.qualityGateStatus()).isEqualTo("OK");
        assertThat(result.bugs()).isEqualTo(5);
        assertThat(result.vulnerabilities()).isEqualTo(1);
        assertThat(result.codeSmells()).isEqualTo(10);
    }

    @Test
    void analyzeRepository_shouldCombineTechStackWithFrameworks() {
        when(cacheService.findCached(repository)).thenReturn(Optional.empty());
        when(client.getProjectMetrics(anyString())).thenReturn(scannedMetrics);
        when(client.getQualityGateStatus(anyString())).thenReturn("OK");
        when(frameworkDetector.detectFrameworks(any(), anyString()))
                .thenReturn(List.of("Spring", "Docker", "PostgreSQL"));

        SonarMetrics result = sonarQubeService.analyzeRepository(repository, "scan-123", "/tmp/repo");

        assertThat(result.techStack()).containsExactlyInAnyOrder(
                "Docker", "Java", "Kotlin", "PostgreSQL", "Spring"
        );
    }

    @Test
    void analyzeRepository_shouldRemoveDuplicateTechStackEntries() {

        SonarMetrics metricsWithDuplicates = new SonarMetrics(
                "OK", 5, 1, 10, 85.5, 3.2, 1000,
                "A", "B", "A", List.of("Java", "Spring Boot")
        );

        when(cacheService.findCached(repository)).thenReturn(Optional.empty());
        when(client.getProjectMetrics(anyString())).thenReturn(metricsWithDuplicates);
        when(client.getQualityGateStatus(anyString())).thenReturn("OK");
        when(frameworkDetector.detectFrameworks(any(), anyString()))
                .thenReturn(List.of("Spring", "Spring Boot", "Java"));

        SonarMetrics result = sonarQubeService.analyzeRepository(repository, "scan-123", "/tmp/repo");

        assertThat(result.techStack()).containsExactlyInAnyOrder("Java", "Spring", "Spring Boot");
    }

    @Test
    void analyzeRepository_shouldThrowException_whenCloneFails() {
        when(cacheService.findCached(repository)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Clone failed"))
                .when(cloner).cloneRepository(anyString(), anyString());

        assertThatThrownBy(() -> sonarQubeService.analyzeRepository(repository, "scan-123", "/tmp/repo"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Repository analysis failed");
    }

    @Test
    void analyzeRepository_shouldThrowException_whenScanFails() {
        when(cacheService.findCached(repository)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Scan failed"))
                .when(scanner).executeScan(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> sonarQubeService.analyzeRepository(repository, "scan-123", "/tmp/repo"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Repository analysis failed");
    }

    @Test
    void analyzeRepository_shouldGenerateUniqueProjectKey() {
        when(cacheService.findCached(repository)).thenReturn(Optional.empty());
        when(client.getProjectMetrics(anyString())).thenReturn(scannedMetrics);
        when(client.getQualityGateStatus(anyString())).thenReturn("OK");
        when(frameworkDetector.detectFrameworks(any(), anyString())).thenReturn(List.of());

        sonarQubeService.analyzeRepository(repository, "scan-123-abc-def", "/tmp/repo");

        verify(scanner).executeScan(
                eq("scan-123_testuser_test-repo"),
                eq("test-repo"),
                eq("/tmp/repo")
        );
    }

    @Test
    void analyzeRepository_shouldSanitizeProjectKey() {
        GitHubRepository repoWithSpecialChars = new GitHubRepository(
                "repo/with*special?chars",
                "user/repo/with*special?chars",
                "https://github.com/user/repo.git",
                new GitHubRepository.Owner("user@domain"),
                false,
                1024,
                10,
                5,
                "Java",
                List.of("Java"),
                Instant.now(),
                Instant.now(),
                "Test",
                true,
                3,
                "main",
                100,
                List.of()
        );

        when(cacheService.findCached(repoWithSpecialChars)).thenReturn(Optional.empty());
        when(client.getProjectMetrics(anyString())).thenReturn(scannedMetrics);
        when(client.getQualityGateStatus(anyString())).thenReturn("OK");
        when(frameworkDetector.detectFrameworks(any(), anyString())).thenReturn(List.of());

        sonarQubeService.analyzeRepository(repoWithSpecialChars, "scan-123", "/tmp/repo");

        verify(scanner).executeScan(
                eq("scan-123_user_domain_repo_with_special_chars"),
                eq("repo/with*special?chars"),
                eq("/tmp/repo")
        );
    }

    @Test
    void analyzeRepository_shouldHandleNullOwner() {
        GitHubRepository repoNoOwner = new GitHubRepository(
                "test-repo",
                "test-repo",
                "https://github.com/test-repo.git",
                null,
                false,
                1024,
                10,
                5,
                "Java",
                List.of("Java"),
                Instant.now(),
                Instant.now(),
                "Test",
                true,
                3,
                "main",
                100,
                List.of()
        );

        when(cacheService.findCached(repoNoOwner)).thenReturn(Optional.empty());
        when(client.getProjectMetrics(anyString())).thenReturn(scannedMetrics);
        when(client.getQualityGateStatus(anyString())).thenReturn("OK");
        when(frameworkDetector.detectFrameworks(any(), anyString())).thenReturn(List.of());

        sonarQubeService.analyzeRepository(repoNoOwner, "scan-123", "/tmp/repo");

        verify(scanner).executeScan(
                eq("scan-123_unknown_test-repo"),
                eq("test-repo"),
                eq("/tmp/repo")
        );
    }
}
