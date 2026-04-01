package ru.devanalyzer.gateway_service.exception;

public class InvalidAnalysisRequestException extends RuntimeException {
    public InvalidAnalysisRequestException(String message) {
        super(message);
    }
}
