package ru.devanalyzer.analytic_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.devanalyzer.analytic_service.dto.AnalysisReportResponse;
import ru.devanalyzer.analytic_service.dto.AnalysisResultDto;
import ru.devanalyzer.analytic_service.dto.AnalysisSummaryDto;
import ru.devanalyzer.analytic_service.dto.GitHubRepoDto;
import ru.devanalyzer.analytic_service.dto.GitHubStatsDto;
import ru.devanalyzer.analytic_service.dto.RepositoryScanResultDto;
import ru.devanalyzer.analytic_service.dto.TechStackAnalysisDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalysisResultRepository {

    private final JdbcTemplate clickHouseJdbcTemplate;
    private final ObjectMapper objectMapper;

    public void save(AnalysisResultDto result) {
        String sql = """
                INSERT INTO analysis_results (
                    request_id, user_id, github_username,
                    total_repositories, filtered_repositories, verified_repositories,
                    successful_scans, failed_scans, overall_score,
                    total_bugs, total_vulnerabilities, total_code_smells,
                    average_coverage, passed_quality_gate, failed_quality_gate,
                    median_security_rating, median_reliability_rating, median_maintainability_rating,
                    repositories_json, tech_stack_json, github_stats_json, github_repo_json,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        clickHouseJdbcTemplate.update(sql,
                result.requestId(),
                result.userId(),
                result.githubUsername(),
                result.totalRepositories(),
                result.filteredRepositories(),
                result.verifiedRepositories(),
                result.successfulScans(),
                result.failedScans(),
                result.overallScore(),
                result.summary() != null ? result.summary().totalBugs() : 0,
                result.summary() != null ? result.summary().totalVulnerabilities() : 0,
                result.summary() != null ? result.summary().totalCodeSmells() : 0,
                result.summary() != null ? result.summary().averageCoverage() : 0.0,
                result.summary() != null ? result.summary().passedQualityGate() : 0,
                result.summary() != null ? result.summary().failedQualityGate() : 0,
                result.summary() != null ? result.summary().medianSecurityRating() : null,
                result.summary() != null ? result.summary().medianReliabilityRating() : null,
                result.summary() != null ? result.summary().medianMaintainabilityRating() : null,
                toJson(result.repositories()),
                toJson(result.techStackAnalysis()),
                toJson(result.gitHubStats()),
                toJson(result.gitHubRepo()),
                Instant.now()
        );
    }

    public Optional<AnalysisReportResponse> findByRequestId(String requestId) {
        String sql = """
                SELECT request_id, user_id, github_username,
                       total_repositories, filtered_repositories, verified_repositories,
                       successful_scans, failed_scans, overall_score,
                       total_bugs, total_vulnerabilities, total_code_smells,
                       average_coverage, passed_quality_gate, failed_quality_gate,
                       median_security_rating, median_reliability_rating, median_maintainability_rating,
                       repositories_json, tech_stack_json, github_stats_json, github_repo_json,
                       created_at
                FROM analysis_results
                WHERE request_id = ?
                LIMIT 1
                """;

        List<AnalysisReportResponse> results = clickHouseJdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    AnalysisSummaryDto summary = new AnalysisSummaryDto(
                            rs.getInt("total_bugs"),
                            rs.getInt("total_vulnerabilities"),
                            rs.getInt("total_code_smells"),
                            rs.getDouble("average_coverage"),
                            rs.getInt("passed_quality_gate"),
                            rs.getInt("failed_quality_gate"),
                            0, 0, 0, 0, 0, 0,
                            rs.getString("median_security_rating"),
                            rs.getString("median_reliability_rating"),
                            rs.getString("median_maintainability_rating")
                    );

                    return new AnalysisReportResponse(
                            rs.getString("request_id"),
                            rs.getLong("user_id"),
                            rs.getString("github_username"),
                            rs.getInt("total_repositories"),
                            rs.getInt("filtered_repositories"),
                            rs.getInt("verified_repositories"),
                            rs.getLong("successful_scans"),
                            rs.getLong("failed_scans"),
                            rs.getInt("overall_score"),
                            summary,
                            fromJson(rs.getString("tech_stack_json"), TechStackAnalysisDto.class),
                            fromJson(rs.getString("repositories_json"), new TypeReference<List<RepositoryScanResultDto>>() {}),
                            fromJson(rs.getString("github_stats_json"), GitHubStatsDto.class),
                            fromJson(rs.getString("github_repo_json"), new TypeReference<List<GitHubRepoDto>>() {}),
                            rs.getTimestamp("created_at").toInstant()
                    );
                }, requestId);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return "{}";
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON to {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON", e);
            return null;
        }
    }
}
