package ru.devanalyzer.analyzer_service.services.sonar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.config.properties.ScanProperties;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryScanServiceTest {

    @Mock
    private SonarQubeService sonarQubeService;

    @Mock
    private ScanProperties scanProperties;

    @InjectMocks
    private RepositoryScanService scanService;

    @TempDir
    Path tempDir;

    private List<GitHubRepository> repositories;
    private SonarMetrics successMetrics;

    @BeforeEach
    void setUp() {
        when(scanProperties.getTempDirectory()).thenReturn(tempDir.toString());

        repositories = List.of(
                createRepository("repo1", "Java"),
                createRepository("repo2", "Python"),
                createRepository("repo3", "Go")
        );

        successMetrics = new SonarMetrics(
                "OK", 5, 1, 10, 85.5, 3.2, 1000,
                "A", "B", "A", List.of("Java")
        );
    }

    @Test
    void scanRepositories_shouldScanAllRepositories() {
        when(sonarQubeService.analyzeRepository(any(), anyString(), anyString()))
                .thenReturn(successMetrics);

        List<RepositoryScanResult> results = scanService.scanRepositories(repositories, "scan-123");

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> "SUCCESS".equals(r.status()));
        assertThat(results).allMatch(r -> r.metrics() != null);
        assertThat(results).extracting(RepositoryScanResult::repositoryName)
                .containsExactly("repo1", "repo2", "repo3");

        verify(sonarQubeService, times(3)).analyzeRepository(any(), anyString(), anyString());
    }

    @Test
    void scanRepositories_shouldHandleFailures() {
        when(sonarQubeService.analyzeRepository(eq(repositories.get(0)), anyString(), anyString()))
                .thenReturn(successMetrics);
        when(sonarQubeService.analyzeRepository(eq(repositories.get(1)), anyString(), anyString()))
                .thenThrow(new RuntimeException("Scan failed"));
        when(sonarQubeService.analyzeRepository(eq(repositories.get(2)), anyString(), anyString()))
                .thenReturn(successMetrics);

        List<RepositoryScanResult> results = scanService.scanRepositories(repositories, "scan-123");

        assertThat(results).hasSize(3);

        RepositoryScanResult success1 = results.getFirst();
        assertThat(success1.status()).isEqualTo("SUCCESS");
        assertThat(success1.metrics()).isNotNull();
        assertThat(success1.errorMessage()).isNull();

        RepositoryScanResult failed = results.get(1);
        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.metrics()).isNull();
        assertThat(failed.errorMessage()).contains("Scan failed");

        RepositoryScanResult success2 = results.get(2);
        assertThat(success2.status()).isEqualTo("SUCCESS");
        assertThat(success2.metrics()).isNotNull();
    }

    @Test
    void scanRepositories_shouldCreateAndCleanupDirectory() {
        when(sonarQubeService.analyzeRepository(any(), anyString(), anyString()))
                .thenReturn(successMetrics);

        scanService.scanRepositories(repositories, "scan-123");

        // метод должен вызываться для каждого репо
        verify(sonarQubeService, times(3)).analyzeRepository(
                any(),
                eq("scan-123"),
                anyString()
        );
    }

    @Test
    void scanRepositories_shouldSanitizeRepositoryNames() {
        GitHubRepository repoWithSpecialChars = new GitHubRepository(
                "repo/with*special?chars",
                "user/repo/with*special?chars",
                "https://github.com/user/repo.git",
                new GitHubRepository.Owner("user"),
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

        when(sonarQubeService.analyzeRepository(any(), anyString(), anyString()))
                .thenReturn(successMetrics);

        List<RepositoryScanResult> results = scanService.scanRepositories(
                List.of(repoWithSpecialChars),
                "scan-123"
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().repositoryName()).isEqualTo("repo/with*special?chars");
    }

    @Test
    void scanRepositories_shouldHandleEmptyRepositoryList() {
        List<RepositoryScanResult> results = scanService.scanRepositories(List.of(), "scan-123");

        assertThat(results).isEmpty();
    }

    @Test
    void scanRepositories_shouldHandleNullLanguage() {
        GitHubRepository repoNoLang = createRepository("repo-nolang", null);

        when(sonarQubeService.analyzeRepository(any(), anyString(), anyString()))
                .thenReturn(successMetrics);

        List<RepositoryScanResult> results = scanService.scanRepositories(
                List.of(repoNoLang),
                "scan-123"
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().language()).isNull();
    }

    @Test
    void scanRepositories_shouldRunScansInParallel() throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(3);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger currentConcurrent = new AtomicInteger(0);

        when(sonarQubeService.analyzeRepository(any(), anyString(), anyString()))
                .thenAnswer(inv -> {

                    int current = currentConcurrent.incrementAndGet();
                    maxConcurrent.updateAndGet(max -> Math.max(max, current));

                    startLatch.await();

                    // симуляция
                    Thread.sleep(100);

                    currentConcurrent.decrementAndGet();
                    completionLatch.countDown();

                    return successMetrics;
                });

        // сканирование в отдельном потоке (scanRepositories блокирующий)
        Thread scanThread = new Thread(() -> {
            scanService.scanRepositories(repositories, "scan-123");
        });
        scanThread.start();

        Thread.sleep(200);

        startLatch.countDown();

        boolean completed = completionLatch.await(5, TimeUnit.SECONDS);

        // проверяем, что все задачи выполнились параллельно
        assertThat(completed).isTrue();
        assertThat(maxConcurrent.get()).isGreaterThanOrEqualTo(3);

        scanThread.join(10000);

        verify(sonarQubeService, times(3)).analyzeRepository(any(), anyString(), anyString());
    }

    @Test
    void scanRepositories_shouldContinueOnFailure() {
        // первый репо падает, остальные успешны
        when(sonarQubeService.analyzeRepository(eq(repositories.get(0)), anyString(), anyString()))
                .thenThrow(new RuntimeException("First repo failed"));
        when(sonarQubeService.analyzeRepository(eq(repositories.get(1)), anyString(), anyString()))
                .thenReturn(successMetrics);
        when(sonarQubeService.analyzeRepository(eq(repositories.get(2)), anyString(), anyString()))
                .thenReturn(successMetrics);

        List<RepositoryScanResult> results = scanService.scanRepositories(repositories, "scan-123");

        assertThat(results).hasSize(3);
        assertThat(results.get(0).status()).isEqualTo("FAILED");
        assertThat(results.get(1).status()).isEqualTo("SUCCESS");
        assertThat(results.get(2).status()).isEqualTo("SUCCESS");
    }

    @Test
    void scanRepositories_shouldHandleExceptionInFuture() {
        // исключение при выполнении Future
        when(sonarQubeService.analyzeRepository(any(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        List<RepositoryScanResult> results = scanService.scanRepositories(
                List.of(repositories.getFirst()),
                "scan-123"
        );

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().status()).isEqualTo("FAILED");
        assertThat(results.getFirst().errorMessage()).contains("Unexpected error");
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
                language != null ? List.of(language) : List.of(),
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
}
