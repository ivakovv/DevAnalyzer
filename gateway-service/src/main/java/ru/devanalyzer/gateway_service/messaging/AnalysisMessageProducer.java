package ru.devanalyzer.gateway_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisRequestDto;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;
import ru.devanalyzer.gateway_service.repository.AnalysisStatusRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisMessageProducer {

    private final KafkaTemplate<String, AnalysisRequestDto> kafkaTemplate;
    private final AnalysisStatusRepository statusRepository;

    @Value("${kafka.topics.analysis-request}")
    private String analysisRequestTopic;

    public void sendAnalysisRequest(AnalysisRequestDto request) {
        kafkaTemplate.send(analysisRequestTopic, request.requestId(), request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send analysis request to Kafka for requestId: {}, error: {}", 
                                request.requestId(), ex.getMessage());
                        statusRepository.saveStatus(request.requestId(), AnalysisStatus.FAILED);
                    } else {
                        log.info("Analysis request sent to Kafka successfully: {}", request.requestId());
                    }
                });
    }
}
