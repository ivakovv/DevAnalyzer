package ru.devanalyzer.analytic_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devanalyzer.analytic_service.dto.*;
import ru.devanalyzer.analytic_service.exception.AccessDeniedException;
import ru.devanalyzer.analytic_service.service.AnalyticService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticController.class)
@Import(TestConfig.class)
class AnalyticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // Вместо deprecated @MockBean
    private AnalyticService analyticService;

    private AnalysisReportResponse sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = createSampleReport();
    }

    @Test
    void getReport_WithValidUserId_ShouldReturnReport() throws Exception {
        // Given
        String requestId = "req-123";
        Long userId = 100L;

        when(analyticService.getReport(eq(requestId), eq(userId)))
                .thenReturn(Optional.of(sampleReport));

        // When & Then
        mockMvc.perform(get("/api/reports/{requestId}", requestId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-123"))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.githubUsername").value("john_doe"))
                .andExpect(jsonPath("$.totalRepositories").value(15))
                .andExpect(jsonPath("$.overallScore").value(85));
    }

    @Test
    void getReport_WithoutUserIdHeader_ShouldReturn403() throws Exception {
        // Given
        String requestId = "req-123";

        when(analyticService.getReport(eq(requestId), isNull()))
                .thenThrow(new AccessDeniedException("Access denied"));

        // When & Then
        mockMvc.perform(get("/api/reports/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReport_WithInvalidUserId_ShouldReturn403() throws Exception {
        // Given
        String requestId = "req-123";
        Long userId = 999L;

        when(analyticService.getReport(eq(requestId), eq(userId)))
                .thenThrow(new AccessDeniedException("Access denied"));

        // When & Then
        mockMvc.perform(get("/api/reports/{requestId}", requestId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReport_WhenReportNotFound_ShouldReturn404() throws Exception {
        // Given
        String requestId = "non-existent";
        Long userId = 100L;

        when(analyticService.getReport(eq(requestId), eq(userId)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/reports/{requestId}", requestId)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
