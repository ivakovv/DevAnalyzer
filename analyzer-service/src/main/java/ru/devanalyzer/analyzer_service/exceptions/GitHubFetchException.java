package ru.devanalyzer.analyzer_service.exceptions;

public class GitHubFetchException extends RuntimeException {
    public GitHubFetchException(String username, Throwable cause) {
        super("Failed to fetch GitHub data for user: " + username, cause);
    }
}
