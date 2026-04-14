package ru.devanalyzer.analytic_service.dto;

public record GitHubRepoDto(
        String name,
        String description,
        String url,
        int stars,
        int forks
) {}
