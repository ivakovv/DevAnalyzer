package ru.devanalyzer.analyzer_service.clients.sonar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.util.TechnologyNameFormatter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SonarMetricsParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SonarMetrics parse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode measures = root.path("component").path("measures");

            MetricsBuilder builder = new MetricsBuilder();

            for (JsonNode measure : measures) {
                String metric = measure.path("metric").asText();
                String value = measure.path("value").asText();
                builder.addMetric(metric, value);
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing metrics", e);
            return createEmptyMetrics();
        }
    }

    public String parseQualityGateStatus(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("projectStatus").path("status").asText("UNKNOWN");
        } catch (Exception e) {
            log.error("Error parsing quality gate status", e);
            return "UNKNOWN";
        }
    }

    private SonarMetrics createEmptyMetrics() {
        return new SonarMetrics(
                "UNKNOWN", null, null, null, null, null, null,
                null, null, null, List.of()
        );
    }

    private static class MetricsBuilder {
        private Integer bugs;
        private Integer vulnerabilities;
        private Integer codeSmells;
        private Double coverage;
        private Double duplications;
        private Integer linesOfCode;
        private String securityRating;
        private String reliabilityRating;
        private String maintainabilityRating;
        private List<String> technologies = new ArrayList<>();

        public void addMetric(String metric, String value) {
            switch (metric) {
                case "bugs" -> bugs = parseInteger(value);
                case "vulnerabilities" -> vulnerabilities = parseInteger(value);
                case "code_smells" -> codeSmells = parseInteger(value);
                case "coverage" -> coverage = parseDouble(value);
                case "duplicated_lines_density" -> duplications = parseDouble(value);
                case "ncloc" -> linesOfCode = parseInteger(value);
                case "security_rating" -> securityRating = convertRatingToLetter(value);
                case "reliability_rating" -> reliabilityRating = convertRatingToLetter(value);
                case "sqale_rating" -> maintainabilityRating = convertRatingToLetter(value);
                case "ncloc_language_distribution" -> technologies = parseLanguageDistribution(value);
            }
        }

        public SonarMetrics build() {
            return new SonarMetrics(
                    "UNKNOWN", 
                    bugs, vulnerabilities, codeSmells,
                    coverage, duplications, linesOfCode,
                    securityRating, reliabilityRating, maintainabilityRating,
                    TechnologyNameFormatter.formatTechnologies(technologies)
            );
        }

        private List<String> parseLanguageDistribution(String distribution) {
            if (distribution == null || distribution.isEmpty()) {
                return List.of();
            }

            List<String> languages = new ArrayList<>();
            String[] parts = distribution.split(";");

            for (String part : parts) {
                String[] langData = part.split("=");
                if (langData.length == 2) {
                    String lang = langData[0].trim();
                    if (!lang.isEmpty() && !lang.equals("<null>") && !lang.equalsIgnoreCase("null")) {
                        languages.add(lang);
                    }
                }
            }

            return languages;
        }

        private String convertRatingToLetter(String numericRating) {
            if (numericRating == null || numericRating.isEmpty()) {
                return null;
            }
            return switch (numericRating) {
                case "1.0", "1" -> "A";
                case "2.0", "2" -> "B";
                case "3.0", "3" -> "C";
                case "4.0", "4" -> "D";
                case "5.0", "5" -> "E";
                default -> null;
            };
        }

        private Integer parseInteger(String value) {
            try {
                return value != null && !value.isEmpty() ? Integer.parseInt(value) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private Double parseDouble(String value) {
            try {
                return value != null && !value.isEmpty() ? Double.parseDouble(value) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
