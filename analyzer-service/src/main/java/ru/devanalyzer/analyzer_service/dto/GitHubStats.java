package ru.devanalyzer.analyzer_service.dto;

public record GitHubStats(long githubId, int repositories, int stars, int forks, int followers, int commits, long ageInDays) {
}
