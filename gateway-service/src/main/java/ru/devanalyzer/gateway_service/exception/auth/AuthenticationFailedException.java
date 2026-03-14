package ru.devanalyzer.gateway_service.exception.auth;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
