package ru.devanalyzer.analyzer_service.services.git;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.config.properties.ScanProperties;

import java.io.File;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitRepositoryCloner {

    private final ScanProperties scanProperties;

    public void cloneRepository(String cloneUrl, String targetDirectory) {
        int maxRetries = 3;
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            attempt++;
            log.info("Cloning repository: {} to {} (attempt {}/{})", 
                    cloneUrl, targetDirectory, attempt, maxRetries);
            
            try {
                cloneWithTimeout(cloneUrl, targetDirectory);
                log.info("Successfully cloned repository: {} on attempt {}", cloneUrl, attempt);
                return;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("Clone attempt {} failed for repository: {}, error: {}", 
                        attempt, cloneUrl, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // 2s, 4s, 8s
                        long waitTime = (long) Math.pow(2, attempt) * 1000;
                        log.info("Waiting {}ms before retry...", waitTime);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Clone interrupted", ie);
                    }
                }
            }
        }
        
        log.error("Failed to clone repository after {} attempts: {}", maxRetries, cloneUrl);
        throw new RuntimeException("Clone failed after " + maxRetries + " attempts: " + 
                (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }
    
    private void cloneWithTimeout(String cloneUrl, String targetDirectory) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            try (Git git = Git.cloneRepository()
                    .setURI(cloneUrl)
                    .setDirectory(new File(targetDirectory))
                    .setCloneAllBranches(false)
                    .setDepth(1)
                    .call()) {
                return null;
            } catch (GitAPIException e) {
                throw new RuntimeException("Git clone failed: " + e.getMessage(), e);
            }
        });

        try {
            future.get(scanProperties.getCloneTimeoutMinutes(), TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Clone timeout after " + scanProperties.getCloneTimeoutMinutes() + " minutes");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Clone failed: " + e.getMessage(), e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
