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

    public AnalysisResponseDto startAnalysis(String githubUsername, List<String> languages, List<String> techStack, Long userId) {
        AnalysisRequestValidator.validate(githubUsername, techStack);

        Optional<Object> cachedResult = resultRepository.getResult(githubUsername, techStack, Object.class);
        if (cachedResult.isPresent()) {
            return handleCachedResult(githubUsername, userId);
        }

        return startNewAnalysis(githubUsername, languages, techStack, userId);
    }

    private AnalysisResponseDto handleCachedResult(String githubUsername, Long userId) {
        String requestId = requestIdGenerator.generate();
        log.info("Cache hit for github: {}, userId: {}, returning cached result with new requestId: {}",
                githubUsername, userId, requestId);

        statusRepository.saveStatus(requestId, userId, AnalysisStatus.COMPLETED);

        return buildResponse(requestId, userId, AnalysisStatus.COMPLETED);
    }

    private AnalysisResponseDto startNewAnalysis(String githubUsername, List<String> languages, List<String> techStack, Long userId) {
        String requestId = requestIdGenerator.generate();

        log.info("Cache miss for github: {}, userId: {}, starting new analysis, requestId: {}",
                githubUsername, userId, requestId);

        statusRepository.saveStatus(requestId, userId, AnalysisStatus.PROCESSING);

        AnalysisRequestDto request = new AnalysisRequestDto(
                requestId,
                userId,
                githubUsername,
                languages,
                techStack,
                Instant.now()
        );
        messageProducer.sendAnalysisRequest(request);

        return buildResponse(requestId, userId, AnalysisStatus.PROCESSING);
    }

    private AnalysisResponseDto buildResponse(String requestId, Long userId, AnalysisStatus status) {
        return new AnalysisResponseDto(
                requestId,
                userId,
                status.getValue(),
                "ws://localhost:" + serverPort + "/ws/analysis/" + requestId,
                OffsetDateTime.now()
        );
    }
}
