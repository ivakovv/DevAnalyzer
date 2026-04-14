package ru.devanalyzer.analytic_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analytic_service.dto.AnalysisResultDto;
import ru.devanalyzer.analytic_service.service.AnalyticService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisResultConsumer {

    private final AnalyticService analyticService;

    @KafkaListener(
            topics = "${kafka.topics.analysis-result}",
            groupId = "analytic-service-group"
    )
    public void consume(AnalysisResultDto result) {
        log.info("Received analysis result: requestId={}, user={}",
                result.requestId(), result.githubUsername());
        analyticService.process(result);
    }
}
