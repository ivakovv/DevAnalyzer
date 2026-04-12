package ru.devanalyzer.analyzer_service.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sonarqube")
public class SonarQubeProperties {
    private String hostUrl;
    private String token;
}
