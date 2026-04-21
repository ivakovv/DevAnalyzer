package ru.devanalyzer.analyzer_service.services.analysis.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SummaryCalculatorTest {

    private SummaryCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new SummaryCalculator();
    }

    @Test
    void calculate_shouldReturnZeroValues_whenEmptyList() {
        AnalysisSummary summary = calculator.calculate(List.of());

        assertThat(summary.totalBugs()).isZero();
        assertThat(summary.totalVulnerabilities()).isZero();
        assertThat(summary.totalCodeSmells()).isZero();
        assertThat(summary.averageCoverage()).isZero();
        assertThat(summary.passedQualityGate()).isZero();
        assertThat(summary.failedQualityGate()).isZero();
        assertThat(summary.medianBugs()).isZero();
        assertThat(summary.medianVulnerabilities()).isZero();
        assertThat(summary.medianCodeSmells()).isZero();
        assertThat(summary.medianCoverage()).isZero();
        assertThat(summary.medianDuplications()).isZero();
        assertThat(summary.medianLinesOfCode()).isZero();
    }

    @Test
    void calculate_shouldComputeCorrectTotals() {
        List<RepositoryScanResult> results = createSampleResults();

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.totalBugs()).isEqualTo(30); // 5 + 10 + 15
        assertThat(summary.totalVulnerabilities()).isEqualTo(9); // 1 + 3 + 5
        assertThat(summary.totalCodeSmells()).isEqualTo(90); // 20 + 30 + 40
    }

    @Test
    void calculate_shouldComputeCorrectAverages() {
        List<RepositoryScanResult> results = createSampleResults();

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.averageCoverage()).isCloseTo(80.0, within(0.01)); // (85 + 90 + 65) / 3
    }

    @Test
    void calculate_shouldCountQualityGatesCorrectly() {
        List<RepositoryScanResult> results = createSampleResults();

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.passedQualityGate()).isEqualTo(2);
        assertThat(summary.failedQualityGate()).isEqualTo(1);
    }

    @Test
    void calculate_shouldComputeCorrectMedians() {
        List<RepositoryScanResult> results = createSampleResults();

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.medianBugs()).isEqualTo(10.0);
        assertThat(summary.medianVulnerabilities()).isEqualTo(3.0);
        assertThat(summary.medianCodeSmells()).isEqualTo(30.0);
        assertThat(summary.medianCoverage()).isEqualTo(85.0);
        assertThat(summary.medianDuplications()).isEqualTo(3.2);
        assertThat(summary.medianLinesOfCode()).isEqualTo(5000.0);
    }

    @Test
    void calculate_shouldComputeMedianRatings() {
        List<RepositoryScanResult> results = createSampleResults();

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.medianSecurityRating()).isEqualTo("A");
        assertThat(summary.medianReliabilityRating()).isEqualTo("B");
        assertThat(summary.medianMaintainabilityRating()).isEqualTo("B");
    }

    @Test
    void calculate_shouldHandleNullValuesGracefully() {
        SonarMetrics metricsWithNulls = new SonarMetrics(
                "OK", null, null, null, null, null, null,
                null, null, null, List.of()
        );

        List<RepositoryScanResult> results = List.of(
                new RepositoryScanResult("repo1", "Java", metricsWithNulls, "SUCCESS", null)
        );

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.totalBugs()).isZero();
        assertThat(summary.averageCoverage()).isZero();
        assertThat(summary.medianSecurityRating()).isNull();
    }

    @Test
    void calculate_shouldHandleEvenNumberOfRatings() {
        List<RepositoryScanResult> results = List.of(
                createResultWithRating("A", "A", "A"),
                createResultWithRating("B", "B", "B"),
                createResultWithRating("C", "C", "C"),
                createResultWithRating("D", "D", "D")
        );

        AnalysisSummary summary = calculator.calculate(results);

        assertThat(summary.medianSecurityRating()).isEqualTo("B");
    }

    private List<RepositoryScanResult> createSampleResults() {
        return List.of(
                new RepositoryScanResult(
                        "repo1",
                        "Java",
                        new SonarMetrics(
                                "OK", 5, 1, 20, 85.0, 3.2, 4000,
                                "A", "A", "A", List.of("Java")
                        ),
                        "SUCCESS",
                        null
                ),
                new RepositoryScanResult(
                        "repo2",
                        "Python",
                        new SonarMetrics(
                                "OK", 10, 3, 30, 90.0, 2.5, 5000,
                                "A", "B", "B", List.of("Python")
                        ),
                        "SUCCESS",
                        null
                ),
                new RepositoryScanResult(
                        "repo3",
                        "Go",
                        new SonarMetrics(
                                "ERROR", 15, 5, 40, 65.0, 5.0, 6000,
                                "C", "C", "C", List.of("Go")
                        ),
                        "SUCCESS",
                        null
                )
        );
    }

    private RepositoryScanResult createResultWithRating(String security, String reliability, String maintainability) {
        return new RepositoryScanResult(
                "repo",
                "Java",
                new SonarMetrics(
                        "OK", 0, 0, 0, 0.0, 0.0, 0,
                        security, reliability, maintainability, List.of()
                ),
                "SUCCESS",
                null
        );
    }
}
