package ru.devanalyzer.analytic_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.devanalyzer.analytic_service.dto.AnalysisPreviewDto;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FavoritesRepository {

    private final JdbcTemplate clickHouseJdbcTemplate;

    public void addToFavorites(Long hrUserId, String requestId) {
        String sql = "INSERT INTO favorites (hr_user_id, request_id, added_at) VALUES (?, ?, ?)";
        clickHouseJdbcTemplate.update(sql, hrUserId, requestId, Instant.now());
    }

    public List<AnalysisPreviewDto> getFavorites(Long hrUserId, int limit, int offset) {
        String sql = """
                SELECT r.request_id AS request_id,
                       r.github_username AS github_username,
                       r.overall_score AS overall_score,
                       r.total_repositories AS total_repositories,
                       r.verified_repositories AS verified_repositories,
                       r.successful_scans AS successful_scans,
                       r.created_at AS created_at
                FROM favorites AS f
                INNER JOIN analysis_results AS r ON f.request_id = r.request_id
                WHERE f.hr_user_id = ?
                ORDER BY f.added_at DESC
                LIMIT ? OFFSET ?
                """;

        return clickHouseJdbcTemplate.query(sql,
                (rs, rowNum) -> new AnalysisPreviewDto(
                        rs.getString("request_id"),
                        rs.getString("github_username"),
                        rs.getInt("overall_score"),
                        rs.getInt("total_repositories"),
                        rs.getInt("verified_repositories"),
                        rs.getLong("successful_scans"),
                        rs.getTimestamp("created_at").toInstant()
                ), hrUserId, limit, offset);
    }

    public void removeFromFavorites(String requestId, Long hrUserId) {
        String sql = "DELETE FROM favorites WHERE request_id = ? AND hr_user_id = ?";
        clickHouseJdbcTemplate.update(sql, requestId, hrUserId);
    }

    public boolean isFavorite(Long hrUserId, String requestId) {
        String sql = "SELECT COUNT(*) FROM favorites WHERE hr_user_id = ? AND request_id = ?";
        Integer count = clickHouseJdbcTemplate.queryForObject(sql, Integer.class, hrUserId, requestId);
        return count != null && count > 0;
    }
}
