package ru.devanalyzer.analyzer_service.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.clients.GitHubClient;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.entity.GitHubStatsEntity;
import ru.devanalyzer.analyzer_service.repository.GitHubStatsRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;
    private final GitHubStatsRepository gitHubStatsRepository;

    public void getStatsAndSave(String username) {
        gitHubStatsRepository.save(toEntity(gitHubClient.getGitHubStats(username)));
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
}
