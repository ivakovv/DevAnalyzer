package ru.devanalyzer.analytic_service.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.devanalyzer.analytic_service.dto.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisResultRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AnalysisResultRepository repository;

    private ObjectMapper objectMapper;
    private AnalysisResultDto sampleResult;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        repository = new AnalysisResultRepository(jdbcTemplate, objectMapper);
        sampleResult = createTestAnalysisResult();
    }

    @Test
    void save_ShouldExecuteInsertQuery() {
        // Given - используем Object... как второй аргумент
        doReturn(1).when(jdbcTemplate).update(anyString(), any(Object[].class));

        // When
        repository.save(sampleResult);

        // Then
        verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
    }

    @Test
    void save_WithNullFields_ShouldHandleGracefully() {
        // Given
        AnalysisResultDto result = new AnalysisResultDto(
                "req-null", 200L, "test_user",
                5, 4, 3, 3L, 1L,
                null, null, null, null, null, null,
                "Partial data"
        );

        doReturn(1).when(jdbcTemplate).update(anyString(), any(Object[].class));

        // When
        repository.save(result);

        // Then
        verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
    }

    @Test
    void save_ShouldHandleNullSummaryGracefully() {
        // Given
        AnalysisResultDto result = new AnalysisResultDto(
                "req-no-summary", 300L, "test_user2",
                10, 8, 6, 6L, 2L,
                null, null, null, 85, null, null,
                "No summary"
        );

        doReturn(1).when(jdbcTemplate).update(anyString(), any(Object[].class));

        // When
        repository.save(result);

        // Then
        verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
    }

    @Test
    void save_ShouldPassCorrectParameters() {
        // Given
        doReturn(1).when(jdbcTemplate).update(anyString(), any(Object[].class));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);

        // When
        repository.save(sampleResult);

        // Then
        verify(jdbcTemplate, times(1)).update(sqlCaptor.capture(), paramsCaptor.capture());

        assertThat(sqlCaptor.getValue()).contains("INSERT INTO analysis_results");
        assertThat(paramsCaptor.getValue()).isNotNull();
        assertThat(paramsCaptor.getValue().length).isEqualTo(23);
    }

    private AnalysisResultDto createTestAnalysisResult() {
        AnalysisSummaryDto summary = new AnalysisSummaryDto(
                15, 8, 30, 82.5, 5, 2,
                7.5, 3.2, 18.5, 85.0, 20.0, 1200.0,
                "A", "B", "A"
        );

        TechStackAnalysisDto techStack = new TechStackAnalysisDto(
                List.of("Java", "Kotlin", "Spring"),
                List.of("Java", "Spring"),
                List.of("Kotlin"),
                66
        );

        GitHubStatsDto gitHubStats = new GitHubStatsDto(
                67890L, "test_user", "Test User",
                "Saint Petersburg", "Test Corp", 25, 200, 75, 350, 800, 730L
        );

        return new AnalysisResultDto(
                "test-req-001", 100L, "test_user",
                25, 20, 18, 18L, 2L,
                summary, techStack, List.of(), 92, gitHubStats, List.of(),
                "Analysis completed successfully"
        );
    }
}
