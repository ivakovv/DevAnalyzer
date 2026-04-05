package ru.devanalyzer.gateway_service.messaging;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisRequestDto;
import ru.devanalyzer.gateway_service.exception.KafkaMessagingException;
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

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "sendAnalysisRequestFallback")
    @Retry(name = "kafkaProducer")
    public void sendAnalysisRequest(AnalysisRequestDto request) {
        log.debug("Attempting to send analysis request to Kafka: {}", request.requestId());
        
        try {
            kafkaTemplate.send(analysisRequestTopic, request.requestId(), request)
                    .get();
            
            log.info("Analysis request sent to Kafka successfully: {}", request.requestId());
        } catch (Exception ex) {
            log.error("Failed to send analysis request to Kafka for requestId: {}, error: {}", 
                    request.requestId(), ex.getMessage());
            statusRepository.saveStatus(request.requestId(), request.userId(), AnalysisStatus.FAILED);
            throw new KafkaMessagingException("Failed to send message to Kafka", ex);
        }
    }

    private void sendAnalysisRequestFallback(AnalysisRequestDto request, Exception ex) {
        log.error("Circuit breaker opened for Kafka producer. RequestId: {}, Error: {}", 
                request.requestId(), ex.getMessage(), ex);
        statusRepository.saveStatus(request.requestId(), request.userId(), AnalysisStatus.FAILED);
        throw new KafkaMessagingException("Analysis service temporarily unavailable", ex);
    }
}
