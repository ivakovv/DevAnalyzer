package ru.devanalyzer.analyzer_service.services.sonar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.clients.SonarQubeClient;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;
import ru.devanalyzer.analyzer_service.services.detection.FrameworkDetector;
import ru.devanalyzer.analyzer_service.services.git.GitRepositoryCloner;
import ru.devanalyzer.analyzer_service.util.TechnologyNameFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class SonarQubeService {

    private final GitRepositoryCloner cloner;
    private final SonarScannerExecutor scanner;
    private final SonarQubeClient client;
    private final FrameworkDetector frameworkDetector;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public SonarMetrics analyzeRepository(GitHubRepository repository, String scanId, String baseDirectory) {
        String projectKey = generateProjectKey(repository, scanId);
        String repoDirectory = baseDirectory + "/" + sanitizeRepoName(repository.name());
        
        log.info("Starting full analysis for repository: {}, projectKey: {}", repository.name(), projectKey);
        
        try {
            log.info("Step 1/3: Cloning repository: {}", repository.cloneUrl());
            cloner.cloneRepository(repository.cloneUrl(), repoDirectory);

            log.info("Step 2/3: Running SonarQube scan for: {}", repository.name());
            scanner.executeScan(projectKey, repository.name(), repoDirectory);

            log.info("Step 3/3: Fetching metrics for: {}", repository.name());
            
            CompletableFuture<SonarMetrics> metricsFuture = waitForMetricsAsync(projectKey, 10, 2000);
            SonarMetrics metrics = metricsFuture.join();
            String qualityGate = client.getQualityGateStatus(projectKey);

            List<String> frameworks = frameworkDetector.detectFrameworks(
                    repository.topics(), 
                    repoDirectory
            );
            log.info("Detected frameworks for {}: {}", repository.name(), frameworks);
            
            List<String> techStack = new ArrayList<>(metrics.techStack());
            techStack.addAll(TechnologyNameFormatter.formatFrameworks(frameworks));
            List<String> finalTechStack = techStack.stream()
                    .distinct()
                    .sorted()
                    .toList();
            
            SonarMetrics finalMetrics = new SonarMetrics(
                    qualityGate,
                    metrics.bugs(),
                    metrics.vulnerabilities(),
                    metrics.codeSmells(),
                    metrics.coverage(),
                    metrics.duplications(),
                    metrics.linesOfCode(),
                    metrics.securityRating(),
                    metrics.reliabilityRating(),
                    metrics.maintainabilityRating(),
                    finalTechStack
            );
            
            log.info("Analysis completed for repository: {}, QualityGate: {}, Bugs: {}, Vulnerabilities: {}",
                    repository.name(), qualityGate, metrics.bugs(), metrics.vulnerabilities());
            
            return finalMetrics;
            
        } catch (Exception e) {
            log.error("Failed to analyze repository: {}", repository.name(), e);
            throw new RuntimeException("Repository analysis failed: " + e.getMessage(), e);
        }
    }
    
    private String generateProjectKey(GitHubRepository repository, String scanId) {
        // Формат: scanId_owner_reponame
        String owner = repository.owner() != null ? repository.owner().login() : "unknown";
        return String.format("%s_%s_%s", 
                scanId.substring(0, 8), // Первые 8 символов scanId
                sanitizeForProjectKey(owner),
                sanitizeForProjectKey(repository.name())
        );
    }
    
    private String sanitizeForProjectKey(String input) {
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
    
    private String sanitizeRepoName(String repoName) {
        return repoName.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }

    private CompletableFuture<SonarMetrics> waitForMetricsAsync(String projectKey, int maxAttempts, long initialDelayMs) {
        CompletableFuture<SonarMetrics> future = new CompletableFuture<>();
        AtomicInteger attempt = new AtomicInteger(1);
        
        scheduleMetricsCheck(projectKey, maxAttempts, initialDelayMs, initialDelayMs, attempt, future);
        
        return future;
    }
    
    private void scheduleMetricsCheck(String projectKey, int maxAttempts, long delayMs, 
                                      long nextDelayMs, AtomicInteger attempt, 
                                      CompletableFuture<SonarMetrics> future) {
        scheduler.schedule(() -> {
            try {
                SonarMetrics metrics = client.getProjectMetrics(projectKey);
                
                if (metrics.linesOfCode() != null && metrics.linesOfCode() > 0) {
                    log.info("Metrics ready for project: {} after {} attempts", projectKey, attempt.get());
                    future.complete(metrics);
                    return;
                }
                
                int currentAttempt = attempt.incrementAndGet();
                
                if (currentAttempt > maxAttempts) {
                    log.warn("Metrics not ready after {} attempts for project: {}", maxAttempts, projectKey);
                    future.complete(metrics);
                    return;
                }
                
                log.debug("Metrics not ready yet for project: {}, attempt {}/{}", 
                        projectKey, currentAttempt, maxAttempts);
                
                long newDelay = (long) (nextDelayMs * 1.5);
                scheduleMetricsCheck(projectKey, maxAttempts, newDelay, newDelay, attempt, future);
                
            } catch (Exception e) {
                log.error("Error checking metrics for project: {}", projectKey, e);
                future.completeExceptionally(e);
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }
}
