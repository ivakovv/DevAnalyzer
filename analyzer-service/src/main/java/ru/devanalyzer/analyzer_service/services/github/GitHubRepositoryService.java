package ru.devanalyzer.analyzer_service.services.github;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.clients.GitHubClient;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubRepositoryService {

    private final GitHubClient gitHubClient;
    private String repositoriesQuery;

    public List<GitHubRepository> getUserRepositories(String username) {
        log.atInfo().addKeyValue("user", username).log("fetching repositories via GraphQL");
        
        List<GitHubRepository> repositories = new ArrayList<>();
        String cursor = null;
        
        try {
            String query = getRepositoriesQuery();
            
            while (true) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("username", username);
                if (cursor != null) {
                    variables.put("after", cursor);
                }
                
                JsonNode response = gitHubClient.executeGraphQLQuery(query, variables);
                JsonNode repos = response.path("data").path("user").path("repositories");
                
                JsonNode nodes = repos.path("nodes");
                if (nodes.isMissingNode() || !nodes.isArray() || nodes.isEmpty()) {
                    break;
                }
                
                for (JsonNode repoNode : nodes) {
                    repositories.add(parseRepository(repoNode));
                }
                
                JsonNode pageInfo = repos.path("pageInfo");
                if (pageInfo.path("hasNextPage").asBoolean()) {
                    cursor = pageInfo.path("endCursor").asText();
                } else {
                    break;
                }
            }
            
            log.atInfo()
                    .addKeyValue("user", username)
                    .addKeyValue("count", repositories.size())
                    .log("repositories fetched successfully");
            
            return repositories;
            
        } catch (Exception e) {
            log.atError()
                    .addKeyValue("user", username)
                    .setCause(e)
                    .log("failed to fetch repositories");
            throw new RuntimeException("Failed to fetch repositories for user: " + username, e);
        }
    }

    private String getRepositoriesQuery() throws IOException {
        if (repositoriesQuery == null) {
            ClassPathResource resource = new ClassPathResource("graphql/repositories.graphql");
            repositoriesQuery = resource.getContentAsString(StandardCharsets.UTF_8);
        }
        return repositoriesQuery;
    }

    private GitHubRepository parseRepository(JsonNode node) {
        JsonNode ownerNode = node.path("owner");
        GitHubRepository.Owner owner = null;
        if (!ownerNode.isMissingNode() && !ownerNode.isNull()) {
            owner = new GitHubRepository.Owner(ownerNode.path("login").asText());
        }
        
        String nameWithOwner = node.path("nameWithOwner").asText();
        String cloneUrl = "https://github.com/" + nameWithOwner + ".git";
        
        return new GitHubRepository(
                node.path("name").asText(),
                nameWithOwner,
                cloneUrl,
                owner,
                node.path("isFork").asBoolean(),
                node.path("diskUsage").asInt(),
                node.path("stargazerCount").asInt(),
                node.path("forkCount").asInt(),
                extractLanguage(node),
                extractLanguages(node),
                parseInstant(node.path("pushedAt").asText(null)),
                parseInstant(node.path("createdAt").asText(null)),
                node.path("description").asText(null),
                node.path("hasIssuesEnabled").asBoolean(),
                node.path("issues").path("totalCount").asInt(),
                extractDefaultBranch(node),
                extractTotalCommits(node),
                extractTopics(node)
        );
    }
    
    private List<String> extractTopics(JsonNode repoNode) {
        List<String> topics = new ArrayList<>();
        JsonNode topicsNode = repoNode.path("repositoryTopics").path("nodes");
        
        if (!topicsNode.isMissingNode() && topicsNode.isArray()) {
            for (JsonNode topicNode : topicsNode) {
                String topicName = topicNode.path("topic").path("name").asText(null);
                if (topicName != null && !topicName.isEmpty()) {
                    topics.add(topicName);
                }
            }
        }
        
        return topics;
    }

    private String extractLanguage(JsonNode repoNode) {
        JsonNode langNode = repoNode.path("primaryLanguage");
        if (!langNode.isMissingNode() && !langNode.isNull()) {
            return langNode.path("name").asText(null);
        }
        return null;
    }
    
    private List<String> extractLanguages(JsonNode repoNode) {
        List<String> languages = new ArrayList<>();
        JsonNode languagesNode = repoNode.path("languages").path("edges");
        
        if (!languagesNode.isMissingNode() && languagesNode.isArray()) {
            for (JsonNode edge : languagesNode) {
                String langName = edge.path("node").path("name").asText(null);
                if (langName != null && !langName.isEmpty()) {
                    languages.add(langName);
                }
            }
        }
        
        return languages;
    }

    private String extractDefaultBranch(JsonNode repoNode) {
        JsonNode branchNode = repoNode.path("defaultBranchRef");
        if (!branchNode.isMissingNode() && !branchNode.isNull()) {
            return branchNode.path("name").asText("main");
        }
        return "main";
    }

    private Instant parseInstant(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(dateStr);
        } catch (Exception e) {
            log.atDebug().addKeyValue("date", dateStr).log("failed to parse date");
            return null;
        }
    }
    
    private int extractTotalCommits(JsonNode repoNode) {
        JsonNode defaultBranch = repoNode.path("defaultBranchRef");
        if (defaultBranch.isMissingNode() || defaultBranch.isNull()) {
            return 0;
        }
        
        JsonNode target = defaultBranch.path("target");
        if (target.isMissingNode() || target.isNull()) {
            return 0;
        }
        
        return target.path("history").path("totalCount").asInt(0);
    }
}
