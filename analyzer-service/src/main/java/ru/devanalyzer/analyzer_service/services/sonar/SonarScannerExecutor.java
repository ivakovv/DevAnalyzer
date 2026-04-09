package ru.devanalyzer.analyzer_service.services.sonar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sonarsource.scanner.api.EmbeddedScanner;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.config.properties.SonarQubeProperties;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonarScannerExecutor {

    private final SonarQubeProperties sonarQubeProperties;

    public void executeScan(String projectKey, String projectName, String projectDirectory) {
        log.info("Starting SonarQube scan for project: {} in directory: {}", projectKey, projectDirectory);
        
        Map<String, String> properties = new HashMap<>();
        properties.put("sonar.projectKey", projectKey);
        properties.put("sonar.projectName", projectName);
        properties.put("sonar.projectBaseDir", projectDirectory);
        properties.put("sonar.sources", ".");
        properties.put("sonar.sourceEncoding", "UTF-8");
        properties.put("sonar.java.binaries", projectDirectory);
        properties.put("sonar.host.url", sonarQubeProperties.getHostUrl());
        
        if (sonarQubeProperties.getToken() != null && !sonarQubeProperties.getToken().isEmpty()) {
            properties.put("sonar.token", sonarQubeProperties.getToken());
        }
        
        try {
            EmbeddedScanner scanner = EmbeddedScanner.create(
                    "DevAnalyzer",
                    "1.0",
                    (formattedMessage, level) -> log.debug("SonarScanner: {}", formattedMessage)
            );
            
            scanner.start();
            scanner.execute(properties);
            
            log.info("SonarQube scan completed successfully for project: {}", projectKey);
            
        } catch (Exception e) {
            log.error("Failed to execute SonarQube scan for project: {}", projectKey, e);
            throw new RuntimeException("SonarQube scan execution failed: " + e.getMessage(), e);
        }
    }
}
