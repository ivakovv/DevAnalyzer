package ru.devanalyzer.gateway_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final UserContextInterceptor userContextInterceptor;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(userContextInterceptor));
        return restTemplate;
    }
}
