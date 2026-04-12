package ru.devanalyzer.gateway_service.service.notification.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.devanalyzer.gateway_service.dto.analysis.websocket.AnalysisStatusMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;

    public void sendStatusUpdate(String userId, AnalysisStatusMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/analysis-status",
                message
            );
            
            log.info("Sent WebSocket message to user {}: requestId={}, status={}", 
                userId, message.requestId(), message.status());
                
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to userId={}: {}", 
                userId, e.getMessage(), e);
        }
    }
}
