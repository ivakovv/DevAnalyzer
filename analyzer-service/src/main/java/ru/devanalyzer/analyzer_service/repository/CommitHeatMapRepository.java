package ru.devanalyzer.analyzer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.devanalyzer.analyzer_service.entity.CommitHeatmapEntity;
import java.util.List;

public interface CommitHeatMapRepository extends JpaRepository<CommitHeatmapEntity, Long> {
    List<CommitHeatmapEntity> findByGithubId(Long githubId);

    @Modifying
    @Query("DELETE FROM CommitHeatmapEntity c WHERE c.githubId = :githubId")
    void deleteByGithubId(Long githubId);
}