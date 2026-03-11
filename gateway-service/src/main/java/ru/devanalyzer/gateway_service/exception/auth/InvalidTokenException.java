package ru.devanalyzer.gateway_service.exception.auth;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
