package ru.devanalyzer.gateway_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.devanalyzer.gateway_service.dto.analysis.AnalysisRequest;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.exception.GlobalExceptionHandler;
import ru.devanalyzer.gateway_service.security.UserPrincipal;
import ru.devanalyzer.gateway_service.service.analysis.AnalysisService;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {

    @Mock
    private AnalysisService analysisService;

    @InjectMocks
    private AnalysisController analysisController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(analysisController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "HR",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );
    }

    @Test
    @DisplayName("POST /api/analysis - успешный запуск анализа")
    void startAnalysis_Success() throws Exception {
        AnalysisRequest request = new AnalysisRequest(
                "testuser",
                List.of("Java", "Python"),
                List.of("Spring Boot", "Docker")
        );

        AnalysisResponseDto expectedResponse = new AnalysisResponseDto(
                "req-123", 1L, "processing", OffsetDateTime.now()
        );

        when(analysisService.startAnalysis(anyString(), anyList(), anyList(), any()))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestId").value("req-123"))
                .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    @DisplayName("POST /api/analysis - валидация: пустой GitHub username")
    void startAnalysis_EmptyGithubUsername_BadRequest() throws Exception {

        AnalysisRequest request = new AnalysisRequest(
                "",
                List.of("Java"),
                List.of("Spring")
        );

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analysis - валидация: null GitHub username")
    void startAnalysis_NullGithubUsername_BadRequest() throws Exception {

        AnalysisRequest request = new AnalysisRequest(
                null,
                List.of("Java"),
                List.of("Spring")
        );

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analysis - валидация: пустой tech stack")
    void startAnalysis_EmptyTechStack_BadRequest() throws Exception {

        AnalysisRequest request = new AnalysisRequest(
                "testuser",
                List.of("Java"),
                List.of()
        );

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analysis - валидация: null tech stack")
    void startAnalysis_NullTechStack_BadRequest() throws Exception {

        AnalysisRequest request = new AnalysisRequest(
                "testuser",
                List.of("Java"),
                null
        );

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal)))
                .andExpect(status().isBadRequest());
    }
}
