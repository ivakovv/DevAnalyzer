package ru.devanalyzer.gateway_service.service.notification.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.devanalyzer.gateway_service.dto.analysis.RedisAnalysisMessage;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisherService {
    
    @Value("${spring.data.redis.pubsub.channel}")
    private String channel;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    

    public void publishStatusUpdate(String userId, String requestId, AnalysisStatus status) {
        try {
            RedisAnalysisMessage message = new RedisAnalysisMessage(
                userId,
                requestId,
                status.getValue(),
                Instant.now()
            );
            
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            redisTemplate.convertAndSend(channel, jsonMessage);
            
            log.info("Successfully published to Redis channel '{}': userId={}, requestId={}, status={}", 
                channel, userId, requestId, status);
                
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message for Redis: userId={}, requestId={}", 
                userId, requestId, e);
        } catch (Exception e) {
            log.error("Failed to publish message to Redis: userId={}, requestId={}", 
                userId, requestId, e);
        }
    }

}
