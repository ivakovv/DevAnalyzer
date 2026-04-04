package ru.devanalyzer.analyzer_service.dto.github;

import java.time.Instant;
import java.util.List;

public record GitHubRepository(
        String name,
        String fullName,
        boolean isFork,
        int size,
        int stargazersCount,
        int forksCount,
        String language,
        List<String> languages, 
        Instant pushedAt,
        Instant createdAt,
        String description,
        boolean hasIssues,
        int openIssuesCount,
        String defaultBranch
) {
}
