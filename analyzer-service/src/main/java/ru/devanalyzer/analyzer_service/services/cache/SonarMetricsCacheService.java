package ru.devanalyzer.analyzer_service.services.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.entity.SonarMetricsCacheEntity;
import ru.devanalyzer.analyzer_service.repository.SonarMetricsCacheRepository;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonarMetricsCacheService {

    private final SonarMetricsCacheRepository cacheRepository;

    public Optional<SonarMetrics> findCached(GitHubRepository repository) {
        Instant pushedAt = repository.pushedAt();

        Optional<SonarMetricsCacheEntity> cached = cacheRepository
                .findByRepositoryFullNameAndLastPushedAt(
                        repository.fullName(),
                        pushedAt
                );

        if (cached.isPresent()) {
            log.info("Cache HIT for {} (pushed: {})",
                    repository.fullName(),
                    repository.pushedAt()
            );
            return Optional.of(mapToMetrics(cached.get()));
        }

        log.info("Cache MISS for {} - full analysis required", repository.fullName());
        return Optional.empty();
    }

    public void save(GitHubRepository repository, SonarMetrics metrics) {
        Instant pushedAt = repository.pushedAt();

        SonarMetricsCacheEntity entity = SonarMetricsCacheEntity.builder()
                .repositoryFullName(repository.fullName())
                .branch(repository.defaultBranch())
                .lastPushedAt(pushedAt)
                .qualityGateStatus(metrics.qualityGateStatus())
                .bugs(metrics.bugs())
                .vulnerabilities(metrics.vulnerabilities())
                .codeSmells(metrics.codeSmells())
                .coverage(metrics.coverage())
                .duplications(metrics.duplications())
                .linesOfCode(metrics.linesOfCode())
                .securityRating(metrics.securityRating())
                .reliabilityRating(metrics.reliabilityRating())
                .maintainabilityRating(metrics.maintainabilityRating())
                .techStack(metrics.techStack())
                .build();

        cacheRepository.save(entity);
        log.info("Saved to cache: {} (pushed: {})", repository.fullName(), repository.pushedAt());
    }

    private SonarMetrics mapToMetrics(SonarMetricsCacheEntity entity) {
        return new SonarMetrics(
                entity.getQualityGateStatus(),
                entity.getBugs(),
                entity.getVulnerabilities(),
                entity.getCodeSmells(),
                entity.getCoverage(),
                entity.getDuplications(),
                entity.getLinesOfCode(),
                entity.getSecurityRating(),
                entity.getReliabilityRating(),
                entity.getMaintainabilityRating(),
                entity.getTechStack()
        );
    }
}
