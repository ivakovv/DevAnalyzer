package ru.devanalyzer.analytic_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analytic_service.dto.*;
import ru.devanalyzer.analytic_service.exception.AccessDeniedException;
import ru.devanalyzer.analytic_service.messaging.AnalysisStatusProducer;
import ru.devanalyzer.analytic_service.repository.AnalysisResultRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticServiceTest {

    @Mock
    private AnalysisResultRepository repository;

    @Mock
    private AnalysisStatusProducer statusProducer;

    @InjectMocks
    private AnalyticService analyticService;

    private AnalysisResultDto sampleResult;
    private AnalysisReportResponse sampleReport;

    @BeforeEach
    void setUp() {
        sampleResult = createSampleAnalysisResult();
        sampleReport = createSampleReport();
    }

    @Test
    void process_ShouldSaveResultAndSendCompletedStatus() {
        // Given
        doNothing().when(repository).save(any(AnalysisResultDto.class));
        doNothing().when(statusProducer).sendCompleted(anyString(), anyLong());

        // When
        analyticService.process(sampleResult);

        // Then
        verify(repository, times(1)).save(sampleResult);
        verify(statusProducer, times(1))
                .sendCompleted(sampleResult.requestId(), sampleResult.userId());
    }

    @Test
    void process_WhenRepositoryThrowsException_ShouldLogErrorAndNotSendStatus() {
        // Given
        doThrow(new RuntimeException("Database error"))
                .when(repository).save(any(AnalysisResultDto.class));

        // When
        analyticService.process(sampleResult);

        // Then
        verify(repository, times(1)).save(sampleResult);
        verify(statusProducer, never()).sendCompleted(anyString(), anyLong());
    }

    @Test
    void getReport_WithValidUserId_ShouldReturnReport() {
        // Given
        String requestId = "req-123";
        Long userId = 100L;

        when(repository.findByRequestId(requestId))
                .thenReturn(Optional.of(sampleReport));

        // When
        Optional<AnalysisReportResponse> result =
                analyticService.getReport(requestId, userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(userId);
        verify(repository, times(1)).findByRequestId(requestId);
    }

    @Test
    void getReport_WithNullUserId_ShouldThrowAccessDeniedException() {
        // Given
        String requestId = "req-123";
        when(repository.findByRequestId(requestId))
                .thenReturn(Optional.of(sampleReport));

        // When & Then
        assertThatThrownBy(() -> analyticService.getReport(requestId, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");

        verify(repository, times(1)).findByRequestId(requestId);
    }

    @Test
    void getReport_WithMismatchedUserId_ShouldThrowAccessDeniedException() {
        // Given
        String requestId = "req-123";
        Long requestingUserId = 999L;

        when(repository.findByRequestId(requestId))
                .thenReturn(Optional.of(sampleReport));

        // When & Then
        assertThatThrownBy(() -> analyticService.getReport(requestId, requestingUserId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied");

        verify(repository, times(1)).findByRequestId(requestId);
    }

    @Test
    void getReport_WhenReportNotFound_ShouldReturnEmpty() {
        // Given
        String requestId = "non-existent";
        Long userId = 100L;

        when(repository.findByRequestId(requestId))
                .thenReturn(Optional.empty());

        // When
        Optional<AnalysisReportResponse> result =
                analyticService.getReport(requestId, userId);

        // Then
        assertThat(result).isEmpty();
        verify(repository, times(1)).findByRequestId(requestId);
    }

    private AnalysisResultDto createSampleAnalysisResult() {
        AnalysisSummaryDto summary = new AnalysisSummaryDto(
                10, 5, 20, 75.5, 3, 2,
                5.0, 2.5, 15.0, 80.0, 25.0, 1000.0,
                "A", "A", "A"
        );

        TechStackAnalysisDto techStack = new TechStackAnalysisDto(
                List.of("Java", "Spring"),
                List.of("Java", "Spring"),
                List.of(),
                100
        );

        GitHubStatsDto gitHubStats = new GitHubStatsDto(
                12345L, "john_doe", "John Doe",
                "Moscow", "Company", 15, 100, 50, 200, 500, 365L
        );

        List<GitHubRepoDto> repos = List.of(
                new GitHubRepoDto("repo1", "Description", "https://github.com/repo1", 10, 5)
        );

        return new AnalysisResultDto(
                "req-123", 100L, "john_doe",
                15, 12, 10, 10L, 2L,
                summary, techStack, List.of(), 85, gitHubStats, repos,
                "Success"
        );
    }

    private AnalysisReportResponse createSampleReport() {
        AnalysisSummaryDto summary = new AnalysisSummaryDto(
                10, 5, 20, 75.5, 3, 2,
                0, 0, 0, 0, 0, 0,
                "A", "A", "A"
        );

        TechStackAnalysisDto techStack = new TechStackAnalysisDto(
                List.of("Java", "Spring"),
                List.of("Java", "Spring"),
                List.of(),
                100
        );

        GitHubStatsDto gitHubStats = new GitHubStatsDto(
                12345L, "john_doe", "John Doe",
                "Moscow", "Company", 15, 100, 50, 200, 500, 365L
        );

        List<GitHubRepoDto> repos = List.of(
                new GitHubRepoDto("repo1", "Description", "https://github.com/repo1", 10, 5)
        );

        return new AnalysisReportResponse(
                "req-123", 100L, "john_doe",
                15, 12, 10, 10L, 2L, 85,
                summary, techStack, List.of(), gitHubStats, repos, Instant.now()
        );
    }
}
