package ru.devanalyzer.gateway_service.validator;

import lombok.extern.slf4j.Slf4j;
import ru.devanalyzer.gateway_service.exception.InvalidAnalysisRequestException;

import java.util.List;

@Slf4j
public final class AnalysisRequestValidator {

    private AnalysisRequestValidator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void validate(String githubUsername, List<String> techStack) {
        if (githubUsername == null || githubUsername.isBlank()) {
            log.warn("Analysis request rejected: github username is required");
            throw new InvalidAnalysisRequestException("GitHub username is required");
        }

        if (techStack == null || techStack.isEmpty()) {
            log.warn("Analysis request rejected: tech stack is required for github: {}", githubUsername);
            throw new InvalidAnalysisRequestException("Tech stack is required for analysis");
        }
    }
}
