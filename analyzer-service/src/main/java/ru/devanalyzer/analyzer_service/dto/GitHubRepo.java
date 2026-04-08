package ru.devanalyzer.analyzer_service.dto;

public record GitHubRepo(
        String name,
        String description,
        String url,
        int stars,
        int forks
) {}
