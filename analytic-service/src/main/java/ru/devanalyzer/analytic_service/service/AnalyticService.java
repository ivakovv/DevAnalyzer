package ru.devanalyzer.analytic_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analytic_service.dto.AnalysisReportResponse;
import ru.devanalyzer.analytic_service.dto.AnalysisResultDto;
import ru.devanalyzer.analytic_service.exception.AccessDeniedException;
import ru.devanalyzer.analytic_service.messaging.AnalysisStatusProducer;
import ru.devanalyzer.analytic_service.repository.AnalysisResultRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticService {

    private final AnalysisResultRepository repository;
    private final AnalysisStatusProducer statusProducer;

    public void process(AnalysisResultDto result) {
        try {

            repository.save(result);

            statusProducer.sendCompleted(result.requestId(), result.userId());

        } catch (Exception e) {
            log.error("Failed to process analysis result: requestId={}", result.requestId(), e);
        }
    }

    public Optional<AnalysisReportResponse> getReport(String requestId, Long requestingUserId) {
        return repository.findByRequestId(requestId)
                .map(report -> {
                    if (requestingUserId == null || !requestingUserId.equals(report.userId())) {
                        log.warn("Access denied: userId={} tried to access report for userId={}",
                                requestingUserId, report.userId());
                        throw new AccessDeniedException("Access denied to report: " + requestId);
                    }
                    return report;
                });
    }
}
