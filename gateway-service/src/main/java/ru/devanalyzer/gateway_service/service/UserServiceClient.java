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
            String url = userServiceUrl + "/internal/users/validate";

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Email", email);
            headers.set("X-Auth-Password", password);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

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
