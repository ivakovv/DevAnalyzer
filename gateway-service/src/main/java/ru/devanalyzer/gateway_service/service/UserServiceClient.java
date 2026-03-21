package ru.devanalyzer.gateway_service.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.devanalyzer.gateway_service.dto.user.UserValidationResponseDto;
import ru.devanalyzer.gateway_service.exception.ServiceUnavailableException;
import ru.devanalyzer.gateway_service.exception.user.PasswordResetException;
import ru.devanalyzer.gateway_service.exception.user.UserNotFoundException;
import ru.devanalyzer.gateway_service.exception.user.UserServiceException;
import ru.devanalyzer.gateway_service.exception.user.UserValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestClient restClient;

    @Value("${user-service.url}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "validateUserFallback")
    @Retry(name = "userService")
    public UserValidationResponseDto validateUser(String email, String password) {
        try {
            String url = userServiceUrl + "/internal/users/validate";

            UserValidationResponseDto response = restClient.post()
                    .uri(url)
                    .header("X-Auth-Email", email)
                    .header("X-Auth-Password", password)
                    .retrieve()
                    .body(UserValidationResponseDto.class);

            log.info("User validation successful for email: {}", email);
            return response;

        } catch (RestClientException e) {
            log.error("Failed to validate user: {}", e.getMessage());
            throw new UserValidationException("Ошибка валидации пользователя", e);
        } catch (Exception e) {
            log.error("Unexpected error during user validation: {}", e.getMessage());
            throw new UserServiceException("Неожиданная ошибка при валидации пользователя", e);
        }
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "findByEmailFallback")
    @Retry(name = "userService")
    public UserValidationResponseDto findByEmail(String email) {
        try {
            String url = userServiceUrl + "/internal/users/by-email?email=" + email;
            
            UserValidationResponseDto response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(UserValidationResponseDto.class);
            
            log.info("User found by email: {}", email);
            return response;
        } catch (RestClientException e) {
            log.error("Failed to find user by email: {}", e.getMessage());
            throw new UserNotFoundException("Пользователь не найден");
        } catch (Exception e) {
            log.error("Unexpected error while finding user: {}", e.getMessage());
            throw new UserServiceException("Неожиданная ошибка при поиске пользователя", e);
        }
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "resetPasswordFallback")
    @Retry(name = "userService")
    public void resetPassword(Long userId, String newPassword) {
        try {
            String url = userServiceUrl + "/internal/users/" + userId + "/reset-password";

            Map<String, String> body = new HashMap<>();
            body.put("newPassword", newPassword);
            
            restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            
            log.info("Password reset successful for userId: {}", userId);
        } catch (RestClientException e) {
            log.error("Failed to reset password for userId {}: {}", userId, e.getMessage());
            throw new PasswordResetException("Ошибка сброса пароля", e);
        } catch (Exception e) {
            log.error("Unexpected error during password reset for userId {}: {}", userId, e.getMessage());
            throw new UserServiceException("Неожиданная ошибка при сбросе пароля", e);
        }
    }

    private UserValidationResponseDto validateUserFallback(String email, String password, Throwable t) {
        log.error("Fallback: User service unavailable for email: {}. Error: {}", email, t.getMessage());
        throw new ServiceUnavailableException("User service is temporarily unavailable. Please try again later.");
    }

    private UserValidationResponseDto findByEmailFallback(String email, Throwable t) {
        log.error("Fallback: Cannot find user by email: {}. Error: {}", email, t.getMessage());
        throw new ServiceUnavailableException("User service is temporarily unavailable. Please try again later.");
    }

    private void resetPasswordFallback(Long userId, String newPassword, Throwable t) {
        log.error("Fallback: Cannot reset password for userId: {}. Error: {}", userId, t.getMessage());
        throw new ServiceUnavailableException("Password reset service is temporarily unavailable. Please try again later.");
    }
}
