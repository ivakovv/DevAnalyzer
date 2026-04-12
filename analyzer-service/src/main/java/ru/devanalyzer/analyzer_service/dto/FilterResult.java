package ru.devanalyzer.analyzer_service.dto;

import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;

public record FilterResult(
        GitHubRepository repository,
        boolean passed,
        String rejectionReason
) {
    public static FilterResult passed(GitHubRepository repository) {
        return new FilterResult(repository, true, null);
    }
    
    public static FilterResult rejected(GitHubRepository repository, String reason) {
        return new FilterResult(repository, false, reason);
    }
}
