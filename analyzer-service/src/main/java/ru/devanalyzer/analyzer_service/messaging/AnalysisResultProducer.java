package ru.devanalyzer.analyzer_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisResultProducer {

    private final KafkaTemplate<String, AnalysisResult> analysisResultKafkaTemplate;

    @Value("${kafka.topics.analysis-result}")
    private String topic;

    public void send(String requestId, AnalysisResult result) {
        log.info("Sending analysis result to topic {}: requestId={}, user={}",
                topic, requestId, result.githubUsername());

        analysisResultKafkaTemplate.send(topic, requestId, result)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send analysis result to topic {}: requestId={}",
                                topic, requestId, ex);
                    } else {
                        log.debug("Successfully sent analysis result to topic {}: requestId={}",
                                topic, requestId);
                    }
                });
    }
}
