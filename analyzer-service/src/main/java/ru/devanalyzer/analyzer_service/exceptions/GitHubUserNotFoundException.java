package ru.devanalyzer.analyzer_service.exceptions;

public class GitHubUserNotFoundException extends RuntimeException {
    public GitHubUserNotFoundException(String username) {
        super("GitHub user not found: " + username);
    }
}
