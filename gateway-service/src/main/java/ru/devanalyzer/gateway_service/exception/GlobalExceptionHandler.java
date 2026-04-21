package ru.devanalyzer.gateway_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.devanalyzer.gateway_service.exception.auth.AuthenticationFailedException;
import ru.devanalyzer.gateway_service.exception.auth.InvalidTokenException;
import ru.devanalyzer.gateway_service.exception.auth.RefreshTokenNotFoundException;
import ru.devanalyzer.gateway_service.exception.error.ErrorResponse;
import ru.devanalyzer.gateway_service.exception.user.PasswordResetException;
import ru.devanalyzer.gateway_service.exception.user.UserNotFoundException;
import ru.devanalyzer.gateway_service.exception.user.UserServiceException;
import ru.devanalyzer.gateway_service.exception.user.UserValidationException;
import ru.devanalyzer.gateway_service.exception.ServiceUnavailableException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        log.error("Refresh token not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        log.error("Invalid token: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(AuthenticationFailedException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                "Access denied. You don't have permission to access this resource.",
                HttpStatus.FORBIDDEN.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex) {
        log.error("Service unavailable: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Circuit breaker or gateway error: {} - {}", ex.getStatusCode(), ex.getReason(), ex);


        ErrorResponse error = new ErrorResponse(
               "Сервис временно недоступен. Пожалуйста, попробуйте позже.",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserValidationException.class)
    public ResponseEntity<ErrorResponse> handleUserValidation(UserValidationException ex) {
        log.error("User validation error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PasswordResetException.class)
    public ResponseEntity<ErrorResponse> handlePasswordReset(PasswordResetException ex) {
        log.error("Password reset error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponse> handleUserService(UserServiceException ex) {
        log.error("User service error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(InvalidAnalysisRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAnalysisRequest(InvalidAnalysisRequestException ex) {
        log.error("Invalid analysis request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(KafkaMessagingException.class)
    public ResponseEntity<ErrorResponse> handleKafkaMessaging(KafkaMessagingException ex) {
        log.error("Kafka messaging error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                "Analysis service is temporarily unavailable. Please try again in a few moments.",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", errorMessage);

        ErrorResponse error = new ErrorResponse(
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                OffsetDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
