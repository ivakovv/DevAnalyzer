package ru.devanalyzer.analyzer_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisResponseDto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisMessageProducer {

    private final KafkaTemplate<String, AnalysisResponseDto> kafkaTemplate;

    public void sendToTopic(String topic, String requestId, Long userId, String status) {
        AnalysisResponseDto response = new AnalysisResponseDto(
                requestId,
                userId,
                status,
                Instant.now()
        );

        log.info("Sending message to topic {}: requestId={}, userId={}, status={}", 
                topic, requestId, userId, status);
        
        kafkaTemplate.send(topic, requestId, response)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send message to topic {}: requestId={}, userId={}", 
                                topic, requestId, userId, ex);
                    } else {
                        log.debug("Successfully sent message to topic {}: requestId={}", topic, requestId);
                    }
                });
    }
}
