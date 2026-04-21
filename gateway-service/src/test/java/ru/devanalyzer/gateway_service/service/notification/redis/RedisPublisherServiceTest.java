package ru.devanalyzer.gateway_service.service.notification.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisPublisherServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisPublisherService redisPublisherService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ReflectionTestUtils.setField(redisPublisherService, "channel", "analysis-status");
        ReflectionTestUtils.setField(redisPublisherService, "objectMapper", objectMapper);
    }

    @Test
    void publishStatusUpdate_Success() {

        String userId = "1";
        String requestId = "req-123";
        AnalysisStatus status = AnalysisStatus.COMPLETED;


        redisPublisherService.publishStatusUpdate(userId, requestId, status);


        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(anyString(), messageCaptor.capture());

        String jsonMessage = messageCaptor.getValue();
        assertThat(jsonMessage).contains("\"userId\":\"1\"");
        assertThat(jsonMessage).contains("\"requestId\":\"req-123\"");
        assertThat(jsonMessage).contains("\"status\":\"completed\"");
        assertThat(jsonMessage).contains("\"timestamp\"");
    }

    @Test
    void publishStatusUpdate_Processing_Success() {

        String userId = "2";
        String requestId = "req-456";
        AnalysisStatus status = AnalysisStatus.PROCESSING;


        redisPublisherService.publishStatusUpdate(userId, requestId, status);


        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(anyString(), messageCaptor.capture());

        String jsonMessage = messageCaptor.getValue();
        assertThat(jsonMessage).contains("\"userId\":\"2\"");
        assertThat(jsonMessage).contains("\"requestId\":\"req-456\"");
        assertThat(jsonMessage).contains("\"status\":\"processing\"");
    }

    @Test
    void publishStatusUpdate_Failed_Success() {

        String userId = "3";
        String requestId = "req-789";
        AnalysisStatus status = AnalysisStatus.FAILED;


        redisPublisherService.publishStatusUpdate(userId, requestId, status);


        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(anyString(), messageCaptor.capture());

        String jsonMessage = messageCaptor.getValue();
        assertThat(jsonMessage).contains("\"userId\":\"3\"");
        assertThat(jsonMessage).contains("\"requestId\":\"req-789\"");
        assertThat(jsonMessage).contains("\"status\":\"failed\"");
    }

    @Test
    void publishStatusUpdate_FullJsonValidation() throws JsonProcessingException {

        String userId = "1";
        String requestId = "req-123";
        AnalysisStatus status = AnalysisStatus.COMPLETED;


        redisPublisherService.publishStatusUpdate(userId, requestId, status);


        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(anyString(), messageCaptor.capture());

        String jsonMessage = messageCaptor.getValue();


        assertThatCode(() -> objectMapper.readTree(jsonMessage))
                .doesNotThrowAnyException();
    }

    @Test
    void publishStatusUpdate_UsesCorrectChannel() {

        String userId = "1";
        String requestId = "req-123";
        AnalysisStatus status = AnalysisStatus.COMPLETED;


        redisPublisherService.publishStatusUpdate(userId, requestId, status);


        verify(redisTemplate).convertAndSend(eq("analysis-status"), anyString());
    }

    @Test
    void publishStatusUpdate_RedisError_LogsError() {

        String userId = "1";
        String requestId = "req-123";
        AnalysisStatus status = AnalysisStatus.COMPLETED;

        doThrow(new RuntimeException("Redis connection error"))
                .when(redisTemplate).convertAndSend(anyString(), anyString());


        assertThatCode(() -> redisPublisherService.publishStatusUpdate(userId, requestId, status))
                .doesNotThrowAnyException();


        verify(redisTemplate).convertAndSend(anyString(), anyString());
    }
}
