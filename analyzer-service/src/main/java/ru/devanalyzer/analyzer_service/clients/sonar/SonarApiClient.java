package ru.devanalyzer.analyzer_service.clients.sonar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.devanalyzer.analyzer_service.config.properties.SonarQubeProperties;

import java.util.Base64;

@Slf4j
@Component
public class SonarApiClient {

    private static final String METRICS_KEYS = "bugs,vulnerabilities,code_smells,coverage," +
            "duplicated_lines_density,ncloc,security_rating,reliability_rating,sqale_rating," +
            "ncloc_language_distribution";

    private final RestClient restClient;

    public SonarApiClient(SonarQubeProperties sonarQubeProperties) {
        String auth = sonarQubeProperties.getToken() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        this.restClient = RestClient.builder()
                .baseUrl(sonarQubeProperties.getHostUrl())
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .build();
    }

    public String fetchProjectMetrics(String projectKey) {
        log.info("Fetching metrics for project: {}", projectKey);

        String url = String.format("/api/measures/component?component=%s&metricKeys=%s",
                projectKey, METRICS_KEYS);

        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Error fetching metrics for project: {}", projectKey, e);
            return null;
        }
    }

    public String fetchQualityGateStatus(String projectKey) {
        log.info("Fetching quality gate status for project: {}", projectKey);

        String url = String.format("/api/qualitygates/project_status?projectKey=%s", projectKey);

        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            log.error("Error fetching quality gate for project: {}", projectKey, e);
            return null;
        }
    }
}
