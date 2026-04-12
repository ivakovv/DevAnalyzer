package ru.devanalyzer.analyzer_service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "scoring.penalties")
public class ScorePenaltiesProperties {
    
    private int vulnerabilityPenalty;
    private int vulnerabilityMaxPenalty;
    
    private int bugPenalty;
    private int bugMaxPenalty;
    
    private int codeSmellDivisor;
    private int codeSmellMaxPenalty;
    
    private int duplicationMultiplier;
}
