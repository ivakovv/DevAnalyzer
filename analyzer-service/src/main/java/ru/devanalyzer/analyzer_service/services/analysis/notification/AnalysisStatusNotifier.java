package ru.devanalyzer.analyzer_service.services.analysis.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.AnalysisResult;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.messaging.AnalysisMessageProducer;
import ru.devanalyzer.analyzer_service.model.AnalysisStatus;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStatusNotifier {

    private final AnalysisMessageProducer messageProducer;
    
    @Value("${kafka.topics.analysis-status}")
    private String statusTopic;
    
    @Value("${kafka.topics.analysis-response}")
    private String responseTopic;

    public void notifyStatus(AnalysisRequestDto request, AnalysisStatus status) {
        Map<String, String> statusUpdate = Map.of("status", status.getValue());
        
        messageProducer.sendToTopic(
                statusTopic,
                request.requestId(),
                request.userId(),
                status.getValue(),
                statusUpdate
        );
        
        log.info("Status update sent to {}: requestId={}, status={}", 
                statusTopic, request.requestId(), status.getValue());
    }

    public void notifyCompleted(AnalysisRequestDto request, AnalysisResult result) {
        messageProducer.sendToTopic(
                responseTopic,
                request.requestId(),
                request.userId(),
                AnalysisStatus.COMPLETED.getValue(),
                result
        );
        
        log.info("Analysis completed sent to {}: requestId={}", responseTopic, request.requestId());
    }

    public void notifyFailed(AnalysisRequestDto request, Exception e) {
        Map<String, Object> errorResult = Map.of(
                "error", e.getMessage(),
                "errorType", e.getClass().getSimpleName()
        );

        messageProducer.sendToTopic(
                responseTopic,
                request.requestId(),
                request.userId(),
                AnalysisStatus.FAILED.getValue(),
                errorResult
        );
        
        log.error("Analysis failed sent to {}: requestId={}, error={}", 
                responseTopic, request.requestId(), e.getMessage());
    }
}
