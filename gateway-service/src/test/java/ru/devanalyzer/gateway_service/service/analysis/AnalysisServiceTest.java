package ru.devanalyzer.gateway_service.service.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisRequestDto;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.exception.InvalidAnalysisRequestException;
import ru.devanalyzer.gateway_service.messaging.AnalysisMessageProducer;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;
import ru.devanalyzer.gateway_service.repository.AnalysisStatusRepository;
import ru.devanalyzer.gateway_service.util.RequestIdGenerator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private RequestIdGenerator requestIdGenerator;

    @Mock
    private AnalysisStatusRepository statusRepository;

    @Mock
    private AnalysisMessageProducer messageProducer;

    @InjectMocks
    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(analysisService, "serverPort", "8080");
    }

    @Test
    void startAnalysis_Success() {
        // Given
        String githubUsername = "testuser";
        List<String> languages = List.of("Java", "Python");
        List<String> techStack = List.of("Spring Boot", "Docker");
        Long userId = 1L;
        String requestId = "req-123";

        when(requestIdGenerator.generate()).thenReturn(requestId);

        AnalysisResponseDto response = analysisService.startAnalysis(githubUsername, languages, techStack, userId);

        assertThat(response).isNotNull();
        assertThat(response.requestId()).isEqualTo(requestId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.status()).isEqualTo(AnalysisStatus.PROCESSING.getValue());

        verify(statusRepository).saveStatus(requestId, userId, AnalysisStatus.PROCESSING);

        ArgumentCaptor<AnalysisRequestDto> requestCaptor = ArgumentCaptor.forClass(AnalysisRequestDto.class);
        verify(messageProducer).sendAnalysisRequest(requestCaptor.capture());

        AnalysisRequestDto capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.requestId()).isEqualTo(requestId);
        assertThat(capturedRequest.userId()).isEqualTo(userId);
        assertThat(capturedRequest.githubUsername()).isEqualTo(githubUsername);
        assertThat(capturedRequest.languages()).containsExactlyElementsOf(languages);
        assertThat(capturedRequest.techStack()).containsExactlyElementsOf(techStack);
    }

    @Test
    void startAnalysis_NullLanguages_Success() {

        String githubUsername = "testuser";
        List<String> languages = null;
        List<String> techStack = List.of("Spring Boot");
        Long userId = 1L;
        String requestId = "req-123";

        when(requestIdGenerator.generate()).thenReturn(requestId);

        AnalysisResponseDto response = analysisService.startAnalysis(githubUsername, languages, techStack, userId);

        assertThat(response).isNotNull();
        verify(statusRepository).saveStatus(requestId, userId, AnalysisStatus.PROCESSING);
        verify(messageProducer).sendAnalysisRequest(any(AnalysisRequestDto.class));
    }

    @Test
    void startAnalysis_EmptyGithubUsername_ThrowsException() {

        String githubUsername = "";
        List<String> languages = List.of("Java");
        List<String> techStack = List.of("Spring Boot");
        Long userId = 1L;

        assertThatThrownBy(() -> analysisService.startAnalysis(githubUsername, languages, techStack, userId))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("GitHub username is required");

        verifyNoInteractions(requestIdGenerator, statusRepository, messageProducer);
    }

    @Test
    void startAnalysis_NullGithubUsername_ThrowsException() {

        String githubUsername = null;
        List<String> languages = List.of("Java");
        List<String> techStack = List.of("Spring Boot");
        Long userId = 1L;

        assertThatThrownBy(() -> analysisService.startAnalysis(githubUsername, languages, techStack, userId))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("GitHub username is required");

        verifyNoInteractions(requestIdGenerator, statusRepository, messageProducer);
    }

    @Test
    void startAnalysis_EmptyTechStack_ThrowsException() {

        String githubUsername = "testuser";
        List<String> languages = List.of("Java");
        List<String> techStack = List.of();
        Long userId = 1L;

        assertThatThrownBy(() -> analysisService.startAnalysis(githubUsername, languages, techStack, userId))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("Tech stack is required for analysis");

        verifyNoInteractions(requestIdGenerator, statusRepository, messageProducer);
    }

    @Test
    void startAnalysis_NullTechStack_ThrowsException() {

        String githubUsername = "testuser";
        List<String> languages = List.of("Java");
        List<String> techStack = null;
        Long userId = 1L;

        assertThatThrownBy(() -> analysisService.startAnalysis(githubUsername, languages, techStack, userId))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("Tech stack is required for analysis");

        verifyNoInteractions(requestIdGenerator, statusRepository, messageProducer);
    }
}
