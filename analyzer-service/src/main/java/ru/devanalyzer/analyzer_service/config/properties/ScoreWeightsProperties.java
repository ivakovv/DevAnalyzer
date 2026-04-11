package ru.devanalyzer.analyzer_service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "scoring.weights")
public class ScoreWeightsProperties {
    
    private double qualityGate;
    private double security;
    private double reliability;
    private double maintainability;
    private double coverage;
    private double duplication;
    private double complexity;
}
