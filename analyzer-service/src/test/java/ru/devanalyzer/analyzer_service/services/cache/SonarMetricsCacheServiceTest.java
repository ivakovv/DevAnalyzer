package ru.devanalyzer.analyzer_service.services.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.entity.SonarMetricsCacheEntity;
import ru.devanalyzer.analyzer_service.repository.SonarMetricsCacheRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SonarMetricsCacheServiceTest {

    @Mock
    private SonarMetricsCacheRepository cacheRepository;

    @InjectMocks
    private SonarMetricsCacheService cacheService;

    @Captor
    private ArgumentCaptor<SonarMetricsCacheEntity> entityCaptor;

    private GitHubRepository repository;
    private SonarMetrics metrics;
    private Instant pushedAt;

    @BeforeEach
    void setUp() {
        pushedAt = Instant.parse("2024-01-15T10:00:00Z");

        repository = new GitHubRepository(
                "test-repo",
                "user/test-repo",
                "https://github.com/user/test-repo.git",
                new GitHubRepository.Owner("user"),
                false,
                1024,
                10,
                5,
                "Java",
                List.of("Java"),
                pushedAt,
                Instant.now().minus(365, java.time.temporal.ChronoUnit.DAYS),
                "Test repo",
                true,
                3,
                "main",
                100,
                List.of()
        );

        metrics = new SonarMetrics(
                "OK",
                5,
                1,
                10,
                85.5,
                3.2,
                5000,
                "A",
                "B",
                "A",
                List.of("Java", "Kotlin")
        );
    }

    @Test
    void findCached_shouldReturnMetrics_whenCacheHit() {
        SonarMetricsCacheEntity cachedEntity = SonarMetricsCacheEntity.builder()
                .repositoryFullName("user/test-repo")
                .lastPushedAt(pushedAt)
                .qualityGateStatus("OK")
                .bugs(5)
                .vulnerabilities(1)
                .codeSmells(10)
                .coverage(85.5)
                .duplications(3.2)
                .linesOfCode(5000)
                .securityRating("A")
                .reliabilityRating("B")
                .maintainabilityRating("A")
                .techStack(List.of("Java", "Kotlin"))
                .build();

        when(cacheRepository.findByRepositoryFullNameAndLastPushedAt(
                eq("user/test-repo"), eq(pushedAt)))
                .thenReturn(Optional.of(cachedEntity));

        Optional<SonarMetrics> result = cacheService.findCached(repository);

        assertThat(result).isPresent();
        SonarMetrics found = result.get();
        assertThat(found.qualityGateStatus()).isEqualTo("OK");
        assertThat(found.bugs()).isEqualTo(5);
        assertThat(found.vulnerabilities()).isEqualTo(1);
        assertThat(found.codeSmells()).isEqualTo(10);
        assertThat(found.coverage()).isEqualTo(85.5);
        assertThat(found.duplications()).isEqualTo(3.2);
        assertThat(found.linesOfCode()).isEqualTo(5000);
        assertThat(found.securityRating()).isEqualTo("A");
        assertThat(found.reliabilityRating()).isEqualTo("B");
        assertThat(found.maintainabilityRating()).isEqualTo("A");
        assertThat(found.techStack()).containsExactly("Java", "Kotlin");
    }

    @Test
    void findCached_shouldReturnEmpty_whenCacheMiss() {
        when(cacheRepository.findByRepositoryFullNameAndLastPushedAt(
                eq("user/test-repo"), eq(pushedAt)))
                .thenReturn(Optional.empty());

        Optional<SonarMetrics> result = cacheService.findCached(repository);

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldPersistMetricsCorrectly() {
        cacheService.save(repository, metrics);

        verify(cacheRepository).save(entityCaptor.capture());
        SonarMetricsCacheEntity saved = entityCaptor.getValue();

        assertThat(saved.getRepositoryFullName()).isEqualTo("user/test-repo");
        assertThat(saved.getBranch()).isEqualTo("main");
        assertThat(saved.getLastPushedAt()).isEqualTo(pushedAt);
        assertThat(saved.getQualityGateStatus()).isEqualTo("OK");
        assertThat(saved.getBugs()).isEqualTo(5);
        assertThat(saved.getVulnerabilities()).isEqualTo(1);
        assertThat(saved.getCodeSmells()).isEqualTo(10);
        assertThat(saved.getCoverage()).isEqualTo(85.5);
        assertThat(saved.getDuplications()).isEqualTo(3.2);
        assertThat(saved.getLinesOfCode()).isEqualTo(5000);
        assertThat(saved.getSecurityRating()).isEqualTo("A");
        assertThat(saved.getReliabilityRating()).isEqualTo("B");
        assertThat(saved.getMaintainabilityRating()).isEqualTo("A");
        assertThat(saved.getTechStack()).containsExactly("Java", "Kotlin");
    }

    @Test
    void save_shouldHandleNullValues() {
        SonarMetrics metricsWithNulls = new SonarMetrics(
                null, null, null, null, null,
                null, null, null, null, null, null
        );

        cacheService.save(repository, metricsWithNulls);

        verify(cacheRepository).save(any(SonarMetricsCacheEntity.class));
    }
}
