package ru.devanalyzer.gateway_service.exception.user;

public class PasswordResetException extends RuntimeException {
    public PasswordResetException(String message) {
        super(message);
    }

    public PasswordResetException(String message, Throwable cause) {
        super(message, cause);
    }
}
