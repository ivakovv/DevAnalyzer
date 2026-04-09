package ru.devanalyzer.analyzer_service.services.sonar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.config.properties.ScanProperties;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.sonar.RepositoryScanResult;
import ru.devanalyzer.analyzer_service.dto.sonar.SonarMetrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryScanService {

    private final SonarQubeService sonarQubeService;
    private final ScanProperties scanProperties;
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 8)
    );
    

    public List<RepositoryScanResult> scanRepositories(List<GitHubRepository> repositories, String scanId) {
        String baseDirectory = createScanDirectory(scanId);
        
        try {
            List<CompletableFuture<RepositoryScanResult>> futures = repositories.stream()
                    .map(repo -> CompletableFuture.supplyAsync(() -> 
                        scanSingleRepository(repo, scanId, baseDirectory), executorService))
                    .toList();
            
            List<RepositoryScanResult> results = futures.stream()
                    .map(future -> {
                        try {
                            return future.join();
                        } catch (Exception e) {
                            log.error("Error during repository scan", e);
                            return new RepositoryScanResult(
                                    "unknown",
                                    "unknown",
                                    null,
                                    "FAILED",
                                    "Scan execution failed: " + e.getMessage()
                            );
                        }
                    })
                    .toList();
            
            log.info("All scans completed. Waiting before cleanup...");
            Thread.sleep(3000);
            
            return results;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Scan interrupted", e);
            throw new RuntimeException("Scan interrupted", e);
        } finally {
            cleanupScanDirectory(baseDirectory);
        }
    }
    
    private RepositoryScanResult scanSingleRepository(
            GitHubRepository repo, 
            String scanId, 
            String baseDirectory
    ) {
        log.info("Scanning repository: {}", repo.name());
        
        try {
            SonarMetrics metrics = sonarQubeService.analyzeRepository(repo, scanId, baseDirectory);
            return new RepositoryScanResult(
                    repo.name(),
                    repo.language(),
                    metrics,
                    "SUCCESS",
                    null
            );
        } catch (Exception e) {
            log.error("Failed to scan repository: {}", repo.name(), e);
            return new RepositoryScanResult(
                    repo.name(),
                    repo.language(),
                    null,
                    "FAILED",
                    e.getMessage()
            );
        }
    }
    
    private String createScanDirectory(String scanId) {
        String baseDir = scanProperties.getTempDirectory() + "/" + scanId;
        try {
            Path basePath = Path.of(baseDir);
            if (Files.exists(basePath)) {
                FileUtils.deleteDirectory(basePath.toFile());
                log.info("Removed existing scan directory: {}", baseDir);
            }
            Files.createDirectories(basePath);
            log.info("Created scan directory: {}", baseDir);
            return baseDir;
        } catch (IOException e) {
            log.error("Failed to create scan directory: {}", baseDir, e);
            throw new RuntimeException("Failed to create scan directory", e);
        }
    }
    
    private void cleanupScanDirectory(String directory) {
        int maxRetries = 3;
        int retryDelay = 1500;
        
        for (int i = 0; i < maxRetries; i++) {
            try {
                File dir = new File(directory);
                if (dir.exists()) {
                    FileUtils.deleteDirectory(dir);
                    log.info("Cleaned up scan directory: {}", directory);
                    return;
                }
            } catch (IOException e) {
                if (i < maxRetries - 1) {
                    log.warn("Failed to cleanup scan directory (attempt {}/{}): {}, retrying...", 
                            i + 1, maxRetries, directory);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("Failed to cleanup scan directory after {} attempts: {}", maxRetries, directory, e);
                }
            }
        }
    }
}
