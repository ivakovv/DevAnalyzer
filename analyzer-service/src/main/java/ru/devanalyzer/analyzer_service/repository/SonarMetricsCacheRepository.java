package ru.devanalyzer.analyzer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.devanalyzer.analyzer_service.entity.SonarMetricsCacheEntity;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface SonarMetricsCacheRepository extends JpaRepository<SonarMetricsCacheEntity, Long> {
    
    Optional<SonarMetricsCacheEntity> findByRepositoryFullNameAndLastPushedAt(
        String repositoryFullName, 
        Instant lastPushedAt
    );
}
