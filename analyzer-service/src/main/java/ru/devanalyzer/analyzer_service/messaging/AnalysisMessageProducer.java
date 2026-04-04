package ru.devanalyzer.analyzer_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisResponseDto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisMessageProducer {

    private final KafkaTemplate<String, AnalysisResponseDto> kafkaTemplate;

    @Value("${kafka.topics.analysis-response}")
    private String responseTopic;

    public void sendAnalysisResponse(String requestId, String status, Object result) {
        AnalysisResponseDto response = new AnalysisResponseDto(
                requestId,
                status,
                result,
                Instant.now()
        );

        log.info("Sending analysis response: requestId={}, status={}", requestId, status);
        
        kafkaTemplate.send(responseTopic, requestId, response)
                .whenComplete((result1, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send analysis response: requestId={}", requestId, ex);
                    } else {
                        log.info("Successfully sent analysis response: requestId={}", requestId);
                    }
                });
    }
}
