package ru.devanalyzer.user_service.exceptions.s3;

public class S3ObjectNotFoundException extends RuntimeException {
    public S3ObjectNotFoundException(String message) {
        super(message);
    }
}
