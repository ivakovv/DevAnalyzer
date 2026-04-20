package ru.devanalyzer.analyzer_service.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devanalyzer.analyzer_service.clients.sonar.SonarApiClient;
import ru.devanalyzer.analyzer_service.clients.sonar.SonarMetricsParser;
import ru.devanalyzer.analyzer_service.config.properties.SonarQubeProperties;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class SonarQubeClientIntegrationTest {

    private WireMockServer wireMockServer;
    private SonarQubeClient sonarQubeClient;
    private SonarApiClient sonarApiClient;
    private SonarMetricsParser metricsParser;
    private SonarQubeProperties sonarQubeProperties;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        sonarQubeProperties = new SonarQubeProperties();
        sonarQubeProperties.setHostUrl("http://localhost:" + wireMockServer.port());
        sonarQubeProperties.setToken("test-token");

        sonarApiClient = new SonarApiClient(sonarQubeProperties);
        metricsParser = new SonarMetricsParser();
        sonarQubeClient = new SonarQubeClient(sonarApiClient, metricsParser);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void getProjectMetrics_shouldReturnMetrics_whenSuccessful() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("test-token:".getBytes());

        String responseBody = """
                {
                    "component": {
                        "measures": [
                            {"metric": "bugs", "value": "15"},
                            {"metric": "vulnerabilities", "value": "3"},
                            {"metric": "code_smells", "value": "42"},
                            {"metric": "coverage", "value": "78.5"},
                            {"metric": "duplicated_lines_density", "value": "5.2"},
                            {"metric": "ncloc", "value": "12500"},
                            {"metric": "security_rating", "value": "1.0"},
                            {"metric": "reliability_rating", "value": "2.0"},
                            {"metric": "sqale_rating", "value": "3.0"},
                            {"metric": "ncloc_language_distribution", "value": "java=10000;js=2500"}
                        ]
                    }
                }
                """;

        stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("component", equalTo("test-project"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        SonarMetrics metrics = sonarQubeClient.getProjectMetrics("test-project");

        assertThat(metrics.bugs()).isEqualTo(15);
        assertThat(metrics.vulnerabilities()).isEqualTo(3);
        assertThat(metrics.codeSmells()).isEqualTo(42);
        assertThat(metrics.coverage()).isEqualTo(78.5);
        assertThat(metrics.duplications()).isEqualTo(5.2);
        assertThat(metrics.linesOfCode()).isEqualTo(12500);
        assertThat(metrics.securityRating()).isEqualTo("A");
        assertThat(metrics.reliabilityRating()).isEqualTo("B");
        assertThat(metrics.maintainabilityRating()).isEqualTo("C");
        assertThat(metrics.techStack()).containsExactly("Java", "JavaScript");
    }

    @Test
    void getProjectMetrics_shouldReturnEmptyMetrics_whenProjectNotFound() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("test-token:".getBytes());

        stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("component", equalTo("nonexistent"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"errors\":[{\"msg\":\"Component not found\"}]}")));

        SonarMetrics metrics = sonarQubeClient.getProjectMetrics("nonexistent");

        assertThat(metrics.qualityGateStatus()).isEqualTo("UNKNOWN");
        assertThat(metrics.bugs()).isNull();
    }

    @Test
    void getQualityGateStatus_shouldReturnStatus_whenSuccessful() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("test-token:".getBytes());

        String responseBody = """
                {
                    "projectStatus": {
                        "status": "OK",
                        "conditions": []
                    }
                }
                """;

        stubFor(get(urlPathEqualTo("/api/qualitygates/project_status"))
                .withQueryParam("projectKey", equalTo("test-project"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        String status = sonarQubeClient.getQualityGateStatus("test-project");

        assertThat(status).isEqualTo("OK");
    }

    @Test
    void getQualityGateStatus_shouldReturnError_whenQualityGateFailed() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString("test-token:".getBytes());

        String responseBody = """
                {
                    "projectStatus": {
                        "status": "ERROR",
                        "conditions": [
                            {"status": "ERROR", "metricKey": "coverage"}
                        ]
                    }
                }
                """;

        stubFor(get(urlPathEqualTo("/api/qualitygates/project_status"))
                .withQueryParam("projectKey", equalTo("test-project"))
                .withHeader("Authorization", equalTo(authHeader))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        String status = sonarQubeClient.getQualityGateStatus("test-project");

        assertThat(status).isEqualTo("ERROR");
    }

    @Test
    void getProjectMetrics_shouldReturnEmptyMetrics_whenUnauthorized() {
        stubFor(get(urlPathEqualTo("/api/measures/component"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("{\"errors\":[{\"msg\":\"Unauthorized\"}]}")));

        SonarMetrics metrics = sonarQubeClient.getProjectMetrics("test-project");

        assertThat(metrics.qualityGateStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void getQualityGateStatus_shouldReturnUnknown_whenUnauthorized() {
        stubFor(get(urlPathEqualTo("/api/qualitygates/project_status"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("{\"errors\":[{\"msg\":\"Unauthorized\"}]}")));

        String status = sonarQubeClient.getQualityGateStatus("test-project");

        assertThat(status).isEqualTo("UNKNOWN");
    }
}
