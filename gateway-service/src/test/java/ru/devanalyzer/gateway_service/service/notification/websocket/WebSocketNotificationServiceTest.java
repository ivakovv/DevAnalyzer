package ru.devanalyzer.gateway_service.service.notification.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.devanalyzer.gateway_service.dto.analysis.websocket.AnalysisStatusMessage;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    @Test
    void sendStatusUpdate_Success() {

        String userId = "1";
        AnalysisStatusMessage message = new AnalysisStatusMessage(
                "req-123",
                "completed",
                Instant.now()
        );


        webSocketNotificationService.sendStatusUpdate(userId, message);


        ArgumentCaptor<AnalysisStatusMessage> messageCaptor = ArgumentCaptor.forClass(AnalysisStatusMessage.class);
        verify(messagingTemplate).convertAndSendToUser(
                eq(userId),
                eq("/queue/analysis-status"),
                messageCaptor.capture()
        );

        AnalysisStatusMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.requestId()).isEqualTo("req-123");
        assertThat(capturedMessage.status()).isEqualTo("completed");
    }

    @Test
    void sendStatusUpdate_Error_LogsError() {

        String userId = "1";
        AnalysisStatusMessage message = new AnalysisStatusMessage(
                "req-123",
                "completed",
                Instant.now()
        );

        doThrow(new RuntimeException("WebSocket error"))
                .when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());


        webSocketNotificationService.sendStatusUpdate(userId, message);


        verify(messagingTemplate).convertAndSendToUser(eq(userId), eq("/queue/analysis-status"), eq(message));
    }
}
