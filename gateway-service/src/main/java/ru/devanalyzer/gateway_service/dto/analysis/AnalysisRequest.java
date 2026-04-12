package ru.devanalyzer.gateway_service.dto.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AnalysisRequest(
        @NotBlank(message = "GitHub username is required")
        String githubUsername,
        
        List<String> languages,  
        
        @NotEmpty(message = "Tech stack is required")
        List<String> techStack   
) {
}
