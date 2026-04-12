package ru.devanalyzer.gateway_service.exception;

public class KafkaMessagingException extends RuntimeException {
    public KafkaMessagingException(String message) {
        super(message);
    }

    public KafkaMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
