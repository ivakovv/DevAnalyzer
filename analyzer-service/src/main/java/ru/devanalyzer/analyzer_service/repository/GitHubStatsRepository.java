package ru.devanalyzer.analyzer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.devanalyzer.analyzer_service.entity.GitHubStatsEntity;

import java.util.Optional;

public interface GitHubStatsRepository extends JpaRepository<GitHubStatsEntity, Long> {
    Optional<GitHubStatsEntity> findByGithubId(Long githubId);
}
