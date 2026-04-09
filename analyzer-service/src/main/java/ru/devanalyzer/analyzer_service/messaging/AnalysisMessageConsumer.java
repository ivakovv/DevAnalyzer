package ru.devanalyzer.analyzer_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.services.core.AnalysisService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisMessageConsumer {

    private final AnalysisService analysisService;

    @KafkaListener(
            topics = "${kafka.topics.analysis-request}",
            groupId = "analyzer-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAnalysisRequest(AnalysisRequestDto request) {
        log.info("Received analysis request: requestId={}, githubUsername={}",
                request.requestId(), request.githubUsername());

        try {
            analysisService.processAnalysisRequest(request);
            log.info("Successfully processed analysis request: requestId={}", request.requestId());
        } catch (Exception e) {
            log.error("Error processing analysis request: requestId={}", request.requestId(), e);
            //TODO Добавить логику отправки ошибки
        }
    }
}
