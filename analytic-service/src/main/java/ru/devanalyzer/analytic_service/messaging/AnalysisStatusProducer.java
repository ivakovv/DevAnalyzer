package ru.devanalyzer.analytic_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analytic_service.dto.AnalysisResponseDto;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStatusProducer {

    private final KafkaTemplate<String, AnalysisResponseDto> kafkaTemplate;

    @Value("${kafka.topics.analysis-status}")
    private String topic;

    public void sendCompleted(String requestId, Long userId) {
        AnalysisResponseDto response = new AnalysisResponseDto(
                requestId,
                userId,
                "completed",
                Instant.now()
        );

        log.info("Sending COMPLETED status: requestId={}, userId={}", requestId, userId);

        kafkaTemplate.send(topic, requestId, response)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send COMPLETED status: requestId={}", requestId, ex);
                    } else {
                        log.debug("COMPLETED status sent: requestId={}", requestId);
                    }
                });
    }
}
