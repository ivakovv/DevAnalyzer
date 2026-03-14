package ru.devanalyzer.gateway_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.devanalyzer.gateway_service.dto.user.UserValidationResponseDto;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${user-service.url}")
    private String userServiceUrl;

    public UserValidationResponseDto validateUser(String email, String password) {

        try {
            String url = userServiceUrl + "/user/internal/validate";
            
            Map<String, String> request = new HashMap<>();
            request.put("email", email);
            request.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            UserValidationResponseDto response = restTemplate.postForObject(
                    url,
                    entity,
                    UserValidationResponseDto.class
            );

            log.info("User validation successful for email: {}", email);
            return response;

        } catch (Exception e) {
            log.error("Failed to validate user: {}", e.getMessage());
            throw new RuntimeException("User validation failed", e);
        }
    }
}
