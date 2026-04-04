package ru.devanalyzer.analyzer_service.clients;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.devanalyzer.analyzer_service.config.GitHubProperties;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GitHubClient {

    private final RestClient restClient;
    private final GitHubProperties gitHubProperties;

    public GitHubClient(@Value("${app.github.token}") String token, GitHubProperties gitHubProperties) {
        this.gitHubProperties = gitHubProperties;
        this.restClient = RestClient.builder()
                .baseUrl(gitHubProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public GitHubStats getGitHubStats(String username) {
        log.atInfo().addKeyValue("user", username).log("fetching stats github");
        try {
            int totalStars = 0;
            int totalForks = 0;
            int repositories = 0;
            int followers = 0;
            int commits = 0;
            long ageInDays = 0;
            long githubId = 0;
            String cursor = null;
            boolean firstPage = true;

            while (true) {
                Map<String, Object> variables = new HashMap<>();
                variables.put("username", username);
                if (cursor != null) variables.put("after", cursor);

                JsonNode user = restClient.post()
                        .uri("/graphql")
                        .body(Map.of("query", gitHubProperties.getQuery(), "variables", variables))
                        .retrieve()
                        .body(JsonNode.class)
                        .path("data").path("user");

                if (firstPage) {
                    firstPage = false;
                    githubId = user.path("databaseId").asLong();
                    repositories = user.path("repositories").path("totalCount").asInt();
                    followers = user.path("followers").path("totalCount").asInt();

                    String createdAt = user.path("createdAt").asText();
                    LocalDate createdDate = LocalDate.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
                    ageInDays = ChronoUnit.DAYS.between(createdDate, LocalDate.now());

                    JsonNode contributions = user.path("contributionsCollection");
                    commits = contributions.path("totalCommitContributions").asInt()
                            + contributions.path("restrictedContributionsCount").asInt();
                }

                for (JsonNode repo : user.path("repositories").path("nodes")) {
                    totalStars += repo.path("stargazerCount").asInt();
                    totalForks += repo.path("forkCount").asInt();
                }

                JsonNode pageInfo = user.path("repositories").path("pageInfo");
                if (pageInfo.path("hasNextPage").asBoolean()) {
                    cursor = pageInfo.path("endCursor").asText();
                } else {
                    break;
                }
            }

            log.atInfo()
                    .addKeyValue("user", username)
                    .addKeyValue("repositories", repositories)
                    .addKeyValue("stars", totalStars)
                    .addKeyValue("forks", totalForks)
                    .addKeyValue("followers", followers)
                    .addKeyValue("commits", commits)
                    .addKeyValue("ageDays", ageInDays)
                    .log("stats from github succes");

            return new GitHubStats(githubId, repositories, totalStars, totalForks, followers, commits, ageInDays);

        } catch (Exception e) {
            log.atError().addKeyValue("user", username).setCause(e).log("faled to fetch github stats");
            throw new RuntimeException("failed to get github stats : " + username);
        }
    }

    public JsonNode executeGraphQLQuery(String query, Map<String, Object> variables) {
        return restClient.post()
                .uri("/graphql")
                .body(Map.of("query", query, "variables", variables))
                .retrieve()
                .body(JsonNode.class);
    }
}
