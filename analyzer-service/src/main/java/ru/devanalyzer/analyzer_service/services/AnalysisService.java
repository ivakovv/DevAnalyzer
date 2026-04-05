package ru.devanalyzer.analyzer_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.messaging.AnalysisMessageProducer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: GitHub API: Подлинность + Активность для всех отфильтрованных репозиториев
// TODO: SonarQube: Технологический стек + Качество кода
// TODO: Аггрегация результатов
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisMessageProducer messageProducer;
    private final RepositoryFilterService filterService;
    private final GitHubRepositoryService gitHubRepositoryService;

    public void processAnalysisRequest(AnalysisRequestDto request) {
        log.info("Processing analysis for user: {}, userId: {}, requestId: {}", 
                request.githubUsername(), request.userId(), request.requestId());
        
        try {
            List<GitHubRepository> allRepositories = gitHubRepositoryService.getUserRepositories(request.githubUsername());
            log.info("Fetched {} repositories for user: {}", allRepositories.size(), request.githubUsername());
            
            List<GitHubRepository> filteredRepositories = filterService.filterRepositories(
                    allRepositories, 
                    request.resumeTechStack()
            );
            log.info("After filtering: {} repositories remain for analysis", filteredRepositories.size());
            
            Map<String, Object> result = buildAnalysisResult(
                    request.githubUsername(),
                    allRepositories.size(),
                    filteredRepositories
            );
            
            messageProducer.sendAnalysisResponse(
                    request.requestId(), 
                    request.userId(),
                    "completed", 
                    result
            );
            
        } catch (Exception e) {
            log.error("Error during analysis processing for user: {}, userId: {}", 
                    request.githubUsername(), request.userId(), e);
            handleAnalysisError(request.requestId(), request.userId(), e);
        }
    }
    
    private Map<String, Object> buildAnalysisResult(
            String githubUsername,
            int totalRepos,
            List<GitHubRepository> filteredRepositories
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("githubUsername", githubUsername);
        result.put("totalRepositories", totalRepos);
        result.put("filteredRepositories", filteredRepositories.size());
        result.put("analyzedRepositories", filteredRepositories.size());
        result.put("repositories", filteredRepositories.stream()
                .map(GitHubRepository::name)
                .toList());
        result.put("status", "filtering_completed");
        result.put("message", "Repository filtering completed. All " + filteredRepositories.size() + " repositories will be analyzed.");
        
        return result;
    }
    
    private void handleAnalysisError(String requestId, Long userId, Exception e) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", e.getMessage());
        errorResult.put("errorType", e.getClass().getSimpleName());

        
        messageProducer.sendAnalysisResponse(
                requestId, 
                userId, 
                "failed", 
                errorResult
        );
    }
}
