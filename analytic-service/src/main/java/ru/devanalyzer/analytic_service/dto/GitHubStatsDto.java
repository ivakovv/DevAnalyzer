package ru.devanalyzer.analytic_service.dto;

public record GitHubStatsDto(
        long githubId,
        String login,
        String name,
        String location,
        String company,
        int repositories,
        int stars,
        int forks,
        int followers,
        int commits,
        long ageInDays
) {}
