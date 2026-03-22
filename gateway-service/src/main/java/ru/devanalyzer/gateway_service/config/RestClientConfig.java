package ru.devanalyzer.gateway_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final UserContextInterceptor userContextInterceptor;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestInterceptor(userContextInterceptor)
                .build();
    }
}
