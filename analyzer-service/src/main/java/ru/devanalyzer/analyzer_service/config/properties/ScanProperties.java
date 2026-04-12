package ru.devanalyzer.analyzer_service.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "scan")
public class ScanProperties {
    private String tempDirectory;
    private int cloneTimeoutMinutes;
    private int scanTimeoutMinutes;
}
