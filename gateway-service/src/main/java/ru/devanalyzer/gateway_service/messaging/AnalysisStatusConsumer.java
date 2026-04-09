package ru.devanalyzer.gateway_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;
import ru.devanalyzer.gateway_service.repository.AnalysisStatusRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStatusConsumer {

    private final AnalysisStatusRepository statusRepository;

    @KafkaListener(
            topics = "analysis-status",
            groupId = "gateway-service-status-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStatusUpdate(AnalysisResponseDto statusUpdate) {
        log.info("Received status update: requestId={}, status={}", 
                statusUpdate.requestId(), statusUpdate.status());

        try {
            AnalysisStatus status = AnalysisStatus.valueOf(statusUpdate.status().toUpperCase());
            statusRepository.saveStatus(
                    statusUpdate.requestId(), 
                    statusUpdate.userId(), 
                    status
            );
            
            log.info("Updated status in Redis for requestId={}: {}", 
                    statusUpdate.requestId(), statusUpdate.status());
                    
        } catch (IllegalArgumentException e) {
            log.warn("Unknown status value: {}, requestId={}", 
                    statusUpdate.status(), statusUpdate.requestId());
        } catch (Exception e) {
            log.error("Error processing status update: requestId={}", 
                    statusUpdate.requestId(), e);
        }
    }
}
