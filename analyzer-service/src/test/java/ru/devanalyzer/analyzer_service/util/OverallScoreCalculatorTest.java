package ru.devanalyzer.analyzer_service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.config.properties.ScoreComplexityProperties;
import ru.devanalyzer.analyzer_service.config.properties.ScorePenaltiesProperties;
import ru.devanalyzer.analyzer_service.config.properties.ScoreWeightsProperties;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OverallScoreCalculatorTest {

    @Mock
    private ScoreWeightsProperties weights;

    @Mock
    private ScorePenaltiesProperties penalties;

    @Mock
    private ScoreComplexityProperties complexityProps;

    @InjectMocks
    private OverallScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        // Используем lenient() для всех stubbing, чтобы избежать UnnecessaryStubbingException
        // когда тест не использует все моки (например, при раннем возврате 0)
        lenient().when(weights.getQualityGate()).thenReturn(0.20);
        lenient().when(weights.getSecurity()).thenReturn(0.20);
        lenient().when(weights.getReliability()).thenReturn(0.15);
        lenient().when(weights.getMaintainability()).thenReturn(0.15);
        lenient().when(weights.getCoverage()).thenReturn(0.15);
        lenient().when(weights.getDuplication()).thenReturn(0.05);
        lenient().when(weights.getComplexity()).thenReturn(0.10);

        // Setup penalty properties
        lenient().when(penalties.getVulnerabilityPenalty()).thenReturn(5);
        lenient().when(penalties.getVulnerabilityMaxPenalty()).thenReturn(30);
        lenient().when(penalties.getBugPenalty()).thenReturn(2);
        lenient().when(penalties.getBugMaxPenalty()).thenReturn(20);
        lenient().when(penalties.getCodeSmellDivisor()).thenReturn(10);
        lenient().when(penalties.getCodeSmellMaxPenalty()).thenReturn(25);
        lenient().when(penalties.getDuplicationMultiplier()).thenReturn(2);

        // Setup complexity properties
        lenient().when(complexityProps.getProjectSizeWeight()).thenReturn(0.6);
        lenient().when(complexityProps.getRepositoryCountWeight()).thenReturn(0.4);
        lenient().when(complexityProps.getMinLinesThreshold()).thenReturn(1000);
        lenient().when(complexityProps.getMaxLinesThreshold()).thenReturn(10000);
        lenient().when(complexityProps.getMinReposThreshold()).thenReturn(3);
        lenient().when(complexityProps.getMaxReposThreshold()).thenReturn(15);
        lenient().when(complexityProps.getCognitiveComplexityWeight()).thenReturn(0.0);
        lenient().when(complexityProps.getFileCountWeight()).thenReturn(0.0);
        lenient().when(complexityProps.getMinFilesThreshold()).thenReturn(10);
        lenient().when(complexityProps.getMaxFilesThreshold()).thenReturn(100);
        lenient().when(complexityProps.getCognitiveComplexityDivisor()).thenReturn(100);
    }

    @Test
    void calculateScore_shouldReturnZero_whenNoRepositoriesScanned() {
        AnalysisSummary summary = new AnalysisSummary(
                0, 0, 0, 0.0, 0, 0,
                0, 0, 0, 0, 0, 0,
                null, null, null
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isZero();
    }

    @Test
    void calculateScore_shouldReturnPerfectScore_whenAllMetricsPerfect() {
        AnalysisSummary summary = new AnalysisSummary(
                0, 0, 0, 100.0, 5, 0,
                0, 0, 0, 100.0, 0, 10000,
                "A", "A", "A"
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isGreaterThan(85);
    }

    @Test
    void calculateScore_shouldReturnLowScore_whenPoorMetrics() {
        AnalysisSummary summary = new AnalysisSummary(
                50, 10, 100, 30.0, 1, 4,
                50, 10, 100, 30.0, 40.0, 500,
                "D", "D", "E"
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isLessThan(50);
    }

    @Test
    void calculateScore_shouldNotExceed100() {
        AnalysisSummary summary = new AnalysisSummary(
                0, 0, 0, 100.0, 10, 0,
                0, 0, 0, 100.0, 0, 20000,
                "A", "A", "A"
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isLessThanOrEqualTo(100);
    }

    @Test
    void calculateScore_shouldNotBeNegative() {
        AnalysisSummary summary = new AnalysisSummary(
                1000, 500, 2000, 0.0, 0, 10,
                1000, 500, 2000, 0.0, 100.0, 100,
                "E", "E", "E"
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isGreaterThanOrEqualTo(0);
    }

    @Test
    void calculateScore_withMixedQualityGates_shouldCalculateCorrectly() {
        AnalysisSummary summary = new AnalysisSummary(
                10, 2, 30, 75.0, 3, 2,
                10, 2, 30, 75.0, 10.0, 5000,
                "B", "B", "C"
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isBetween(50, 80);
    }

    @Test
    void calculateScore_shouldHandleNullRatings() {
        AnalysisSummary summary = new AnalysisSummary(
                10, 2, 30, 75.0, 3, 2,
                10, 2, 30, 75.0, 10.0, 5000,
                null, null, null
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isBetween(0, 100);
    }

    @Test
    void calculateScore_shouldHandleZeroLinesOfCode() {
        AnalysisSummary summary = new AnalysisSummary(
                0, 0, 0, 0.0, 1, 0,
                0, 0, 0, 0.0, 0.0, 0,
                "A", "A", "A"
        );

        int score = calculator.calculateScore(summary);

        assertThat(score).isGreaterThanOrEqualTo(0);
    }
}
