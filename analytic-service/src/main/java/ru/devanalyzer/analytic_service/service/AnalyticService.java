package ru.devanalyzer.analytic_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analytic_service.dto.AnalysisPreviewDto;
import ru.devanalyzer.analytic_service.dto.AnalysisReportResponse;
import ru.devanalyzer.analytic_service.dto.AnalysisResultDto;
import ru.devanalyzer.analytic_service.exception.AccessDeniedException;
import ru.devanalyzer.analytic_service.exception.NotFoundException;
import ru.devanalyzer.analytic_service.messaging.AnalysisStatusProducer;
import ru.devanalyzer.analytic_service.repository.AnalysisResultRepository;
import ru.devanalyzer.analytic_service.repository.FavoritesRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticService {

    private final AnalysisResultRepository repository;
    private final FavoritesRepository favoritesRepository;
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

    public List<AnalysisPreviewDto> getHistory(Long userId, int limit, int offset) {
        return repository.findHistoryByUserId(userId, limit, offset);
    }

    public void addToFavorites(Long userId, String requestId) {
        if (!repository.existsByRequestId(requestId)) {
            throw new NotFoundException("Report not found: " + requestId);
        }
        favoritesRepository.addToFavorites(userId, requestId);
    }

    public List<AnalysisPreviewDto> getFavorites(Long userId, int limit, int offset) {
        return favoritesRepository.getFavorites(userId, limit, offset);
    }

    public boolean isFavorite(Long userId, String requestId) {
        return favoritesRepository.isFavorite(userId, requestId);
    }

    public void removeFromFavorites(Long userId, String requestId) {
        favoritesRepository.removeFromFavorites(requestId, userId);
    }
}
