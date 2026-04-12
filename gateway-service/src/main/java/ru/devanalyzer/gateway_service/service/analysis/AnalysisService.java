package ru.devanalyzer.gateway_service.service.analysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisRequestDto;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.messaging.AnalysisMessageProducer;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;
import ru.devanalyzer.gateway_service.repository.AnalysisStatusRepository;
import ru.devanalyzer.gateway_service.util.RequestIdGenerator;
import ru.devanalyzer.gateway_service.validator.AnalysisRequestValidator;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final RequestIdGenerator requestIdGenerator;
    private final AnalysisStatusRepository statusRepository;
    private final AnalysisMessageProducer messageProducer;

    @Value("${server.port}")
    private String serverPort;

    public AnalysisResponseDto startAnalysis(String githubUsername, List<String> languages, List<String> techStack, Long userId) {
        AnalysisRequestValidator.validate(githubUsername, techStack);

        String requestId = requestIdGenerator.generate();

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
                OffsetDateTime.now()
        );
    }
}
