package ru.devanalyzer.gateway_service.service.notification.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.gateway_service.dto.analysis.RedisAnalysisMessage;
import ru.devanalyzer.gateway_service.dto.analysis.websocket.AnalysisStatusMessage;
import ru.devanalyzer.gateway_service.service.notification.websocket.WebSocketNotificationService;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisSubscriberServiceTest {

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisSubscriberService redisSubscriberService;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ReflectionTestUtils.setField(redisSubscriberService, "objectMapper", objectMapper);
    }

    @Test
    void onMessage_Success() throws Exception {

        RedisAnalysisMessage redisMessage = new RedisAnalysisMessage(
                "1",
                "req-123",
                "completed",
                Instant.now()
        );
        String jsonMessage = objectMapper.writeValueAsString(redisMessage);
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(jsonMessage.getBytes());


        redisSubscriberService.onMessage(message, new byte[0]);


        ArgumentCaptor<AnalysisStatusMessage> messageCaptor =
                ArgumentCaptor.forClass(AnalysisStatusMessage.class);
        verify(webSocketNotificationService).sendStatusUpdate(eq("1"), messageCaptor.capture());

        AnalysisStatusMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.requestId()).isEqualTo("req-123");
        assertThat(capturedMessage.status()).isEqualTo("completed");
    }

    @Test
    void onMessage_DeserializationError_LogsError() {

        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("invalid json".getBytes());


        redisSubscriberService.onMessage(message, new byte[0]);


        verify(webSocketNotificationService, never()).sendStatusUpdate(anyString(), any());
    }

    @Test
    void onMessage_EmptyBody_LogsError() {

        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(new byte[0]);


        redisSubscriberService.onMessage(message, new byte[0]);


        verify(webSocketNotificationService, never()).sendStatusUpdate(anyString(), any());
    }

    @Test
    void onMessage_VariousStatuses_Success() throws Exception {

        RedisAnalysisMessage processingMessage = new RedisAnalysisMessage(
                "1", "req-1", "processing", Instant.now()
        );
        String json1 = objectMapper.writeValueAsString(processingMessage);
        Message message1 = mock(Message.class);
        when(message1.getBody()).thenReturn(json1.getBytes());

        redisSubscriberService.onMessage(message1, new byte[0]);
        verify(webSocketNotificationService, times(1)).sendStatusUpdate(eq("1"), any());


        RedisAnalysisMessage failedMessage = new RedisAnalysisMessage(
                "1", "req-2", "failed", Instant.now()
        );
        String json2 = objectMapper.writeValueAsString(failedMessage);
        Message message2 = mock(Message.class);
        when(message2.getBody()).thenReturn(json2.getBytes());

        redisSubscriberService.onMessage(message2, new byte[0]);
        verify(webSocketNotificationService, times(2)).sendStatusUpdate(eq("1"), any());


        RedisAnalysisMessage filteringMessage = new RedisAnalysisMessage(
                "1", "req-3", "filtering", Instant.now()
        );
        String json3 = objectMapper.writeValueAsString(filteringMessage);
        Message message3 = mock(Message.class);
        when(message3.getBody()).thenReturn(json3.getBytes());

        redisSubscriberService.onMessage(message3, new byte[0]);
        verify(webSocketNotificationService, times(3)).sendStatusUpdate(eq("1"), any());
    }

    @Test
    void onMessage_DifferentUsers_Success() throws Exception {

        RedisAnalysisMessage message1 = new RedisAnalysisMessage(
                "1", "req-1", "completed", Instant.now()
        );
        String json1 = objectMapper.writeValueAsString(message1);
        Message msg1 = mock(Message.class);
        when(msg1.getBody()).thenReturn(json1.getBytes());

        RedisAnalysisMessage message2 = new RedisAnalysisMessage(
                "2", "req-2", "processing", Instant.now()
        );
        String json2 = objectMapper.writeValueAsString(message2);
        Message msg2 = mock(Message.class);
        when(msg2.getBody()).thenReturn(json2.getBytes());


        redisSubscriberService.onMessage(msg1, new byte[0]);
        redisSubscriberService.onMessage(msg2, new byte[0]);


        verify(webSocketNotificationService).sendStatusUpdate(eq("1"), any());
        verify(webSocketNotificationService).sendStatusUpdate(eq("2"), any());
    }

    @Test
    void onMessage_NullBody_NoException() {

        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(null);


        redisSubscriberService.onMessage(message, new byte[0]);


        verify(webSocketNotificationService, never()).sendStatusUpdate(anyString(), any());
    }
}
