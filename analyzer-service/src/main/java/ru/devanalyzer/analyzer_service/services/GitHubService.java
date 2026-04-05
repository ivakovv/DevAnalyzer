package ru.devanalyzer.analyzer_service.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devanalyzer.analyzer_service.clients.GitHubClient;
import ru.devanalyzer.analyzer_service.dto.GitHubRepo;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;

import java.util.List;
import ru.devanalyzer.analyzer_service.dto.WeekActivity;
import ru.devanalyzer.analyzer_service.entity.CommitHeatmapEntity;
import ru.devanalyzer.analyzer_service.entity.GitHubStatsEntity;
import ru.devanalyzer.analyzer_service.repository.CommitHeatMapRepository;
import ru.devanalyzer.analyzer_service.repository.GitHubStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;
    private final GitHubStatsRepository gitHubStatsRepository;
    private final CommitHeatMapRepository heatmapRepository;

    @Transactional
    public List<GitHubRepo> getRepositories(String username) {
        return gitHubClient.getRepositories(username);
    }

    public GitHubStats getStats(String username) {
        Long githubId = gitHubClient.getGithubId(username);

        Optional<GitHubStatsEntity> existing = gitHubStatsRepository.findByGithubId(githubId);

        if (existing.isPresent() && isToday(existing.get().getFetchedAt())) {
            List<CommitHeatmapEntity> heatmap = heatmapRepository.findByGithubId(githubId);
            return toDto(existing.get(), heatmap);
        }

        GitHubStats stats = gitHubClient.getGitHubStats(username);

        GitHubStatsEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setRepositories(stats.repositories());
            entity.setStars(stats.stars());
            entity.setForks(stats.forks());
            entity.setFollowers(stats.followers());
            entity.setCommits(stats.commits());
            entity.setAgeInDays(stats.ageInDays());
            entity.setFetchedAt(LocalDateTime.now());
        } else {
            entity = toEntity(stats);
        }

        gitHubStatsRepository.save(entity);

        heatmapRepository.deleteByGithubId(stats.githubId());
        heatmapRepository.saveAll(toHeatmapEntities(stats.githubId(), stats.heatmap()));

        return stats;
    }

    private boolean isToday(LocalDateTime fetchedAt) {
        return fetchedAt.toLocalDate().equals(LocalDate.now());
    }

    private GitHubStats toDto(GitHubStatsEntity entity, List<CommitHeatmapEntity> heatmap) {
        List<WeekActivity> weeks = heatmap.stream()
                .map(h -> new WeekActivity(h.getWeekStart(), h.getDays(), h.getTotal()))
                .toList();
        return new GitHubStats(
                entity.getGithubId(),
                entity.getRepositories(),
                entity.getStars(),
                entity.getForks(),
                entity.getFollowers(),
                entity.getCommits(),
                entity.getAgeInDays(),
                weeks
        );
    }

    private GitHubStatsEntity toEntity(GitHubStats stats) {
        return GitHubStatsEntity.builder()
                .githubId(stats.githubId())
                .repositories(stats.repositories())
                .stars(stats.stars())
                .forks(stats.forks())
                .followers(stats.followers())
                .commits(stats.commits())
                .ageInDays(stats.ageInDays())
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    private List<CommitHeatmapEntity> toHeatmapEntities(Long githubId, List<WeekActivity> weeks) {
        return weeks.stream()
                .map(week -> CommitHeatmapEntity.builder()
                        .githubId(githubId)
                        .weekStart(week.weekStart())
                        .days(week.days())
                        .total(week.total())
                        .build())
                .toList();
    }
}