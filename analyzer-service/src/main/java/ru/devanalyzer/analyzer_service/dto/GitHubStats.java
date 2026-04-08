package ru.devanalyzer.analyzer_service.dto;

import java.util.List;

public record GitHubStats(
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
        long ageInDays,
        List<WeekActivity> heatmap
) {}