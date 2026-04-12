package ru.devanalyzer.analyzer_service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "scoring.complexity")
public class ScoreComplexityProperties {
    
    private double projectSizeWeight;
    private double cognitiveComplexityWeight;
    private double repositoryCountWeight;
    private double fileCountWeight;
    
    private int minLinesThreshold;
    private int maxLinesThreshold;
    
    private int minReposThreshold;
    private int maxReposThreshold;
    
    private int minFilesThreshold;
    private int maxFilesThreshold;
    
    private int cognitiveComplexityDivisor;
}
