package ru.devanalyzer.gateway_service.service.notification.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import ru.devanalyzer.gateway_service.dto.analysis.RedisAnalysisMessage;
import ru.devanalyzer.gateway_service.dto.analysis.websocket.AnalysisStatusMessage;
import ru.devanalyzer.gateway_service.service.notification.websocket.WebSocketNotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriberService implements MessageListener {
    
    private final WebSocketNotificationService webSocketNotificationService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());

            RedisAnalysisMessage redisMessage =
                objectMapper.readValue(messageBody, RedisAnalysisMessage.class);
            
            AnalysisStatusMessage wsMessage = new AnalysisStatusMessage(
                redisMessage.requestId(),
                redisMessage.status(),
                redisMessage.timestamp()
            );

            webSocketNotificationService.sendStatusUpdate(
                redisMessage.userId(), 
                wsMessage
            );
            
            log.info("Successfully forwarded Redis message to WebSocket: userId={}, requestId={}, status={}", 
                redisMessage.userId(), redisMessage.requestId(), redisMessage.status());
                
        } catch (Exception e) {
            log.error("Failed to process Redis Pub/Sub message: {}", e.getMessage(), e);
        }
    }
}
