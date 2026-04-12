package ru.devanalyzer.analyzer_service.services.analysis.calculator;

import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;

import java.util.List;

@Component
public class ScanResultCounter {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAILED_STATUS = "FAILED";

    public List<RepositoryScanResult> extractSuccessfulResults(List<RepositoryScanResult> scanResults) {
        return scanResults.stream()
                .filter(this::isSuccessful)
                .filter(r -> r.metrics() != null)
                .toList();
    }

    public long countSuccessful(List<RepositoryScanResult> scanResults) {
        return scanResults.stream()
                .filter(this::isSuccessful)
                .count();
    }

    public long countFailed(List<RepositoryScanResult> scanResults) {
        return scanResults.stream()
                .filter(this::isFailed)
                .count();
    }

    private boolean isSuccessful(RepositoryScanResult result) {
        return SUCCESS_STATUS.equals(result.status());
    }

    private boolean isFailed(RepositoryScanResult result) {
        return FAILED_STATUS.equals(result.status());
    }
}
