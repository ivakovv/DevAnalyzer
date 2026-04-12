package ru.devanalyzer.gateway_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;
import ru.devanalyzer.gateway_service.repository.AnalysisStatusRepository;
import ru.devanalyzer.gateway_service.service.notification.redis.RedisPublisherService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStatusConsumer {

    private final AnalysisStatusRepository statusRepository;
    private final RedisPublisherService redisPublisherService;

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
            
            log.info("Updated status to Redis: requestId={}, status={}",
                    statusUpdate.requestId(), status);
            redisPublisherService.publishStatusUpdate(
                    statusUpdate.userId().toString(),
                    statusUpdate.requestId(),
                    status
            );

            log.info("Published to Redis Pub/Sub: requestId={}, status={}",
                    statusUpdate.requestId(), status);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Unknown status value: {}, requestId={}", 
                    statusUpdate.status(), statusUpdate.requestId());
        } catch (Exception e) {
            log.error("Error processing status update: requestId={}", 
                    statusUpdate.requestId(), e);
        }
    }
}
