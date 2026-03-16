package ru.devanalyzer.user_service.exceptions.s3;

public class PresignedUrlGenerationException extends RuntimeException {
    public PresignedUrlGenerationException(String message) {
        super(message);
    }
}
