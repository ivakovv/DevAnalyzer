package ru.devanalyzer.analyzer_service.services.analysis.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.messaging.AnalysisMessageProducer;
import ru.devanalyzer.analyzer_service.model.AnalysisStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStatusNotifier {

    private final AnalysisMessageProducer messageProducer;
    
    @Value("${kafka.topics.analysis-status}")
    private String statusTopic;

    public void notifyStatus(AnalysisRequestDto request, AnalysisStatus status) {
        messageProducer.sendToTopic(
                statusTopic,
                request.requestId(),
                request.userId(),
                status.getValue()
        );
        
        log.info("Status update sent to {}: requestId={}, status={}", 
                statusTopic, request.requestId(), status.getValue());
    }

    public void notifyFailed(AnalysisRequestDto request, Exception e) {
        messageProducer.sendToTopic(
                statusTopic,
                request.requestId(),
                request.userId(),
                AnalysisStatus.FAILED.getValue()
        );
        
        log.error("Analysis failed sent to {}: requestId={}, error={}",
                statusTopic, request.requestId(), e.getMessage());
    }
}
