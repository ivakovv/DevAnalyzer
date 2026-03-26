package ru.devanalyzer.analyzer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.devanalyzer.analyzer_service.entity.GitHubStatsEntity;

public interface GitHubStatsRepository extends JpaRepository<GitHubStatsEntity, Long> {
}
