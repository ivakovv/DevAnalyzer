package ru.devanalyzer.analyzer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.devanalyzer.analyzer_service.entity.CommitHeatmapEntity;
import java.util.List;

public interface CommitHeatMapRepository extends JpaRepository<CommitHeatmapEntity, Long> {
    List<CommitHeatmapEntity> findByGithubId(Long githubId);
    void deleteByGithubId(Long githubId);
}