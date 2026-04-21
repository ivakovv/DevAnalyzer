package ru.devanalyzer.analytic_service.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analytic_service.dto.AnalysisResultDto;
import ru.devanalyzer.analytic_service.dto.AnalysisSummaryDto;
import ru.devanalyzer.analytic_service.dto.GitHubStatsDto;
import ru.devanalyzer.analytic_service.service.AnalyticService;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisResultConsumerTest {

    @Mock
    private AnalyticService analyticService;

    @InjectMocks
    private AnalysisResultConsumer consumer;

    private AnalysisResultDto sampleResult;

    @BeforeEach
    void setUp() {
        sampleResult = createSampleResult();
    }

    @Test
    void consume_ShouldProcessMessage() {
        // When
        consumer.consume(sampleResult);

        // Then
        verify(analyticService, times(1)).process(sampleResult);
    }

    @Test
    void consume_WithMultipleMessages_ShouldProcessAll() {
        // Given
        AnalysisResultDto result2 = createSampleResult();

        // When
        consumer.consume(sampleResult);
        consumer.consume(result2);

        // Then
        verify(analyticService, times(2)).process(any(AnalysisResultDto.class));
    }

    @Test
    void consume_WhenServiceThrowsException_ShouldNotBreakConsumer() {
        // Given
        doThrow(new RuntimeException("Processing error"))
                .when(analyticService).process(any(AnalysisResultDto.class));

        // When & Then - should not throw exception
        consumer.consume(sampleResult);

        verify(analyticService, times(1)).process(sampleResult);
    }

    private AnalysisResultDto createSampleResult() {
        AnalysisSummaryDto summary = new AnalysisSummaryDto(
                10, 5, 20, 75.5, 3, 2,
                5.0, 2.5, 15.0, 80.0, 25.0, 1000.0,
                "A", "A", "A"
        );

        GitHubStatsDto gitHubStats = new GitHubStatsDto(
                12345L, "john_doe", "John Doe",
                "Moscow", "Company", 15, 100, 50, 200, 500, 365L
        );

        return new AnalysisResultDto(
                "req-123", 100L, "john_doe",
                15, 12, 10, 10L, 2L,
                summary, null, List.of(), 85, gitHubStats, List.of(),
                "Success"
        );
    }
}
