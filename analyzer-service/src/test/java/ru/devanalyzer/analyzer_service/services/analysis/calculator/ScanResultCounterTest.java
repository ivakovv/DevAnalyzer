package ru.devanalyzer.analyzer_service.services.analysis.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScanResultCounterTest {

    private ScanResultCounter counter;

    @BeforeEach
    void setUp() {
        counter = new ScanResultCounter();
    }

    @Test
    void extractSuccessfulResults_shouldReturnOnlySuccessfulWithMetrics() {
        SonarMetrics validMetrics = new SonarMetrics(
                "OK", 5, 1, 10, 85.5, 3.2, 1000,
                "A", "B", "A", List.of("Java")
        );

        List<RepositoryScanResult> results = List.of(
                new RepositoryScanResult("repo1", "Java", validMetrics, "SUCCESS", null),
                new RepositoryScanResult("repo2", "Python", null, "SUCCESS", null),
                new RepositoryScanResult("repo3", "Go", validMetrics, "FAILED", "Error"),
                new RepositoryScanResult("repo4", "Rust", validMetrics, "SUCCESS", null)
        );

        List<RepositoryScanResult> successful = counter.extractSuccessfulResults(results);

        assertThat(successful).hasSize(2);
        assertThat(successful).extracting(RepositoryScanResult::repositoryName)
                .containsExactly("repo1", "repo4");
    }

    @Test
    void extractSuccessfulResults_shouldReturnEmptyList_whenNoSuccessfulResults() {
        List<RepositoryScanResult> results = List.of(
                new RepositoryScanResult("repo1", "Java", null, "FAILED", "Error"),
                new RepositoryScanResult("repo2", "Python", null, "FAILED", "Error")
        );

        List<RepositoryScanResult> successful = counter.extractSuccessfulResults(results);

        assertThat(successful).isEmpty();
    }

    @Test
    void countSuccessful_shouldCountCorrectly() {
        List<RepositoryScanResult> results = List.of(
                new RepositoryScanResult("repo1", "Java", null, "SUCCESS", null),
                new RepositoryScanResult("repo2", "Python", null, "SUCCESS", null),
                new RepositoryScanResult("repo3", "Go", null, "FAILED", "Error"),
                new RepositoryScanResult("repo4", "Rust", null, "SUCCESS", null)
        );

        long count = counter.countSuccessful(results);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void countSuccessful_shouldReturnZero_whenEmptyList() {
        long count = counter.countSuccessful(List.of());

        assertThat(count).isZero();
    }

    @Test
    void countFailed_shouldCountCorrectly() {
        List<RepositoryScanResult> results = List.of(
                new RepositoryScanResult("repo1", "Java", null, "SUCCESS", null),
                new RepositoryScanResult("repo2", "Python", null, "FAILED", "Error1"),
                new RepositoryScanResult("repo3", "Go", null, "FAILED", "Error2"),
                new RepositoryScanResult("repo4", "Rust", null, "SUCCESS", null)
        );

        long count = counter.countFailed(results);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countFailed_shouldReturnZero_whenEmptyList() {
        long count = counter.countFailed(List.of());

        assertThat(count).isZero();
    }

    @Test
    void combined_shouldMatchTotalCount() {
        List<RepositoryScanResult> results = List.of(
                new RepositoryScanResult("repo1", "Java", null, "SUCCESS", null),
                new RepositoryScanResult("repo2", "Python", null, "FAILED", "Error1"),
                new RepositoryScanResult("repo3", "Go", null, "FAILED", "Error2"),
                new RepositoryScanResult("repo4", "Rust", null, "SUCCESS", null),
                new RepositoryScanResult("repo5", "C++", null, "SUCCESS", null)
        );

        long successful = counter.countSuccessful(results);
        long failed = counter.countFailed(results);

        assertThat(successful + failed).isEqualTo(results.size());
    }
}
