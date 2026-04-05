package ru.devanalyzer.gateway_service.service.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisRequestDto;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.messaging.AnalysisMessageProducer;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;
import ru.devanalyzer.gateway_service.repository.AnalysisResultRepository;
import ru.devanalyzer.gateway_service.repository.AnalysisStatusRepository;
import ru.devanalyzer.gateway_service.util.RequestIdGenerator;
import ru.devanalyzer.gateway_service.validator.AnalysisRequestValidator;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final RequestIdGenerator requestIdGenerator;
    private final AnalysisStatusRepository statusRepository;
    private final AnalysisResultRepository resultRepository;
    private final AnalysisMessageProducer messageProducer;

    @Value("${server.port}")
    private String serverPort;

    public AnalysisResponseDto startAnalysis(String githubUsername, List<String> resumeTechStack, Long userId) {
        AnalysisRequestValidator.validate(githubUsername, resumeTechStack);

        Optional<Object> cachedResult = resultRepository.getResult(githubUsername, resumeTechStack, Object.class);
        if (cachedResult.isPresent()) {
            return handleCachedResult(githubUsername, userId);
        }

        return startNewAnalysis(githubUsername, resumeTechStack, userId);
    }

    private AnalysisResponseDto handleCachedResult(String githubUsername, Long userId) {
        String requestId = requestIdGenerator.generate();
        log.info("Cache hit for github: {}, userId: {}, returning cached result with new requestId: {}", 
                githubUsername, userId, requestId);
        
        statusRepository.saveStatus(requestId, userId, AnalysisStatus.COMPLETED);
        
        return buildResponse(requestId, AnalysisStatus.COMPLETED);
    }

    private AnalysisResponseDto startNewAnalysis(String githubUsername, List<String> resumeTechStack, Long userId) {
        String requestId = requestIdGenerator.generate();

        log.info("Cache miss for github: {}, userId: {}, starting new analysis, requestId: {}",
                githubUsername, userId, requestId);

        statusRepository.saveStatus(requestId, userId, AnalysisStatus.PROCESSING);

        AnalysisRequestDto request = new AnalysisRequestDto(
                requestId,
                userId,
                githubUsername,
                resumeTechStack,
                Instant.now()
        );
        messageProducer.sendAnalysisRequest(request);

        return buildResponse(requestId, AnalysisStatus.PROCESSING);
    }

    private AnalysisResponseDto buildResponse(String requestId, AnalysisStatus status) {
        return new AnalysisResponseDto(
                requestId,
                status.getValue(),
                "ws://localhost:" + serverPort + "/ws/analysis/" + requestId,
                OffsetDateTime.now()
        );
    }
}
