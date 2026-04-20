package ru.devanalyzer.analytic_service.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.devanalyzer.analytic_service.service.AnalyticService;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public AnalyticService mockAnalyticService() {
        return mock(AnalyticService.class);
    }
}
