package ru.devanalyzer.analyzer_service.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.clients.sonar.SonarApiClient;
import ru.devanalyzer.analyzer_service.clients.sonar.SonarMetricsParser;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class SonarQubeClient {

    private final SonarApiClient apiClient;
    private final SonarMetricsParser metricsParser;

    public SonarMetrics getProjectMetrics(String projectKey) {
        log.info("Getting metrics for project: {}", projectKey);

        String response = apiClient.fetchProjectMetrics(projectKey);

        if (response == null || response.isEmpty()) {
            log.warn("Empty response for project: {}", projectKey);
            return createEmptyMetrics();
        }

        return metricsParser.parse(response);
    }

    public String getQualityGateStatus(String projectKey) {
        log.info("Getting quality gate status for project: {}", projectKey);

        String response = apiClient.fetchQualityGateStatus(projectKey);

        if (response == null || response.isEmpty()) {
            log.warn("Empty quality gate response for project: {}", projectKey);
            return "UNKNOWN";
        }

        return metricsParser.parseQualityGateStatus(response);
    }

    private SonarMetrics createEmptyMetrics() {
        return new SonarMetrics(
                "UNKNOWN", null, null, null, null, null, null,
                null, null, null, List.of()
        );
    }
}
