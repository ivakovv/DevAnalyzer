package ru.devanalyzer.analyzer_service.clients.sonar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import static org.assertj.core.api.Assertions.assertThat;

class SonarMetricsParserTest {

    private SonarMetricsParser parser;

    @BeforeEach
    void setUp() {
        parser = new SonarMetricsParser();
    }

    @Test
    void parse_shouldReturnEmptyMetrics_whenResponseIsEmpty() {
        SonarMetrics result = parser.parse("");

        assertThat(result.qualityGateStatus()).isEqualTo("UNKNOWN");
        assertThat(result.bugs()).isNull();
        assertThat(result.vulnerabilities()).isNull();
    }

    @Test
    void parse_shouldReturnEmptyMetrics_whenResponseIsInvalid() {
        SonarMetrics result = parser.parse("invalid json");

        assertThat(result.qualityGateStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void parse_shouldExtractAllMetrics_whenValidResponse() {
        String jsonResponse = """
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

        SonarMetrics result = parser.parse(jsonResponse);

        assertThat(result.bugs()).isEqualTo(15);
        assertThat(result.vulnerabilities()).isEqualTo(3);
        assertThat(result.codeSmells()).isEqualTo(42);
        assertThat(result.coverage()).isEqualTo(78.5);
        assertThat(result.duplications()).isEqualTo(5.2);
        assertThat(result.linesOfCode()).isEqualTo(12500);
        assertThat(result.securityRating()).isEqualTo("A");
        assertThat(result.reliabilityRating()).isEqualTo("B");
        assertThat(result.maintainabilityRating()).isEqualTo("C");
        assertThat(result.techStack()).containsExactly("Java", "JavaScript");
    }

    @Test
    void parse_shouldHandleMissingMetrics() {
        String jsonResponse = """
                {
                    "component": {
                        "measures": [
                            {"metric": "bugs", "value": "10"},
                            {"metric": "coverage", "value": "85.0"}
                        ]
                    }
                }
                """;

        SonarMetrics result = parser.parse(jsonResponse);

        assertThat(result.bugs()).isEqualTo(10);
        assertThat(result.coverage()).isEqualTo(85.0);
        assertThat(result.vulnerabilities()).isNull();
        assertThat(result.codeSmells()).isNull();
    }

    @Test
    void parse_shouldHandleNullValues() {
        String jsonResponse = """
                {
                    "component": {
                        "measures": [
                            {"metric": "bugs", "value": null},
                            {"metric": "coverage", "value": ""}
                        ]
                    }
                }
                """;

        SonarMetrics result = parser.parse(jsonResponse);

        assertThat(result.bugs()).isNull();
        assertThat(result.coverage()).isNull();
    }

    @Test
    void parseQualityGateStatus_shouldReturnUnknown_whenInvalidResponse() {
        String result = parser.parseQualityGateStatus("invalid");

        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    void parseQualityGateStatus_shouldReturnStatus_whenValidResponse() {
        String jsonResponse = """
                {
                    "projectStatus": {
                        "status": "OK"
                    }
                }
                """;

        String result = parser.parseQualityGateStatus(jsonResponse);

        assertThat(result).isEqualTo("OK");
    }

    @Test
    void parseQualityGateStatus_shouldReturnUnknown_whenStatusMissing() {
        String jsonResponse = """
                {
                    "projectStatus": {}
                }
                """;

        String result = parser.parseQualityGateStatus(jsonResponse);

        assertThat(result).isEqualTo("UNKNOWN");
    }

    @Test
    void parse_shouldConvertRatingsCorrectly() {
        String jsonResponse = """
                {
                    "component": {
                        "measures": [
                            {"metric": "security_rating", "value": "1"},
                            {"metric": "reliability_rating", "value": "2"},
                            {"metric": "sqale_rating", "value": "3"}
                        ]
                    }
                }
                """;

        SonarMetrics result = parser.parse(jsonResponse);

        assertThat(result.securityRating()).isEqualTo("A");
        assertThat(result.reliabilityRating()).isEqualTo("B");
        assertThat(result.maintainabilityRating()).isEqualTo("C");
    }

    @Test
    void parse_shouldFilterOutNullLanguages() {
        String jsonResponse = """
                {
                    "component": {
                        "measures": [
                            {"metric": "ncloc_language_distribution", "value": "java=1000;<null>=500;js=300;null=200"}
                        ]
                    }
                }
                """;

        SonarMetrics result = parser.parse(jsonResponse);

        assertThat(result.techStack()).containsExactly("Java", "JavaScript");
    }

    @Test
    void parse_shouldReturnEmptyTechStack_whenDistributionEmpty() {
        String jsonResponse = """
                {
                    "component": {
                        "measures": [
                            {"metric": "ncloc_language_distribution", "value": ""}
                        ]
                    }
                }
                """;

        SonarMetrics result = parser.parse(jsonResponse);

        assertThat(result.techStack()).isEmpty();
    }
}
