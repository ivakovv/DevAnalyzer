package ru.devanalyzer.analyzer_service.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.clients.GitHubClient;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorshipVerificationService {
    
    private final GitHubClient gitHubClient;
    
    @Value("${analyzer.authorship.min-ownership-percentage}")
    private double minOwnershipPercentage;

    private String authorshipQuery;
    
    /**
     * Проверяет, является ли владелец основным автором репозитория.
     * Формула: (коммиты владельца / все коммиты) * 100 >= минимальный порог
     */
    public boolean verifyOwnership(GitHubRepository repository, String ownerUsername) {
        try {
            int totalCommits = 0;
            int ownerCommits = 0;
            String cursor = null;
            boolean hasNextPage = true;
            
            while (hasNextPage) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("owner", ownerUsername);
                variables.put("name", repository.name());
                if (cursor != null) {
                    variables.put("after", cursor);
                }
                
                JsonNode response = gitHubClient.executeGraphQLQuery(
                        getAuthorshipQuery(),
                        variables
                );
                
                JsonNode history = response.path("data")
                        .path("repository")
                        .path("defaultBranchRef")
                        .path("target")
                        .path("history");
                
                if (totalCommits == 0) {
                    totalCommits = history.path("totalCount").asInt(0);
                    if (totalCommits == 0) {
                        log.debug("Repository '{}' has no commits, skipping ownership check", repository.name());
                        return true; 
                    }
                }
                
                JsonNode nodes = history.path("nodes");
                for (JsonNode node : nodes) {
                    String authorLogin = node.path("author")
                            .path("user")
                            .path("login")
                            .asText(null);
                    
                    if (ownerUsername.equalsIgnoreCase(authorLogin)) {
                        ownerCommits++;
                    }
                }
                
                JsonNode pageInfo = history.path("pageInfo");
                hasNextPage = pageInfo.path("hasNextPage").asBoolean(false);
                cursor = pageInfo.path("endCursor").asText(null);
            }
            
            double ownerPercentage = (ownerCommits * 100.0) / totalCommits;
            boolean isOwner = ownerPercentage >= minOwnershipPercentage;
            
            log.info("Repository '{}': owner commits {}/{} ({:.1f}%), threshold: {:.1f}%, ownership: {}", 
                    repository.name(), 
                    ownerCommits, 
                    totalCommits,
                    ownerPercentage,
                    minOwnershipPercentage,
                    isOwner);
            
            return isOwner;
            
        } catch (Exception e) {
            log.error("Failed to verify ownership for repository '{}': {}", 
                    repository.name(), e.getMessage());
            return true;
        }

    }

    private String getAuthorshipQuery() throws IOException {
        if (authorshipQuery == null) {
            ClassPathResource resource = new ClassPathResource("graphql/authorship.graphql");
            authorshipQuery = resource.getContentAsString(StandardCharsets.UTF_8);
        }
        return authorshipQuery;
    }
}
