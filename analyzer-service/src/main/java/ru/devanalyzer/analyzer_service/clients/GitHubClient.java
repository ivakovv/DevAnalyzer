package ru.devanalyzer.analyzer_service.clients;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.devanalyzer.analyzer_service.config.GitHubProperties;
import ru.devanalyzer.analyzer_service.dto.GitHubRepo;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.dto.WeekActivity;
import ru.devanalyzer.analyzer_service.exceptions.GitHubFetchException;
import ru.devanalyzer.analyzer_service.exceptions.GitHubUserNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public List<GitHubRepo> getRepositories(String username) {
        log.atInfo().addKeyValue("user", username).log("fetching repositories");
        List<GitHubRepo> repos = new ArrayList<>();
        String cursor = null;

        while (true) {
            JsonNode reposNode = executeQuery(gitHubProperties.getReposQuery(), username, cursor)
                    .path("data").path("user").path("repositories");

            for (JsonNode node : reposNode.path("nodes")) {
                repos.add(mapRepo(node));
            }

            JsonNode pageInfo = reposNode.path("pageInfo");
            if (pageInfo.path("hasNextPage").asBoolean()) {
                cursor = pageInfo.path("endCursor").asText();
            } else {
                break;
            }
        }

        return repos;
    }

    public GitHubStats getGitHubStats(String username) {
        log.atInfo().addKeyValue("user", username).log("fetching stats github");
        try {
            JsonNode firstPage = executeQuery(gitHubProperties.getQuery(), username, null).path("data").path("user");
            int[] starsForks = countStarsForks(username, firstPage);

            return new GitHubStats(
                    firstPage.path("databaseId").asLong(),
                    firstPage.path("login").asText(),
                    firstPage.path("name").asText(null),
                    firstPage.path("location").asText(null),
                    firstPage.path("company").asText(null),
                    firstPage.path("repositories").path("totalCount").asInt(),
                    starsForks[0],
                    starsForks[1],
                    firstPage.path("followers").path("totalCount").asInt(),
                    parseCommits(firstPage.path("contributionsCollection")),
                    parseAgeInDays(firstPage.path("createdAt").asText()),
                    parseHeatmap(firstPage.path("contributionsCollection"))
            );
        } catch (GitHubUserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.atError().addKeyValue("user", username).setCause(e).log("failed to fetch github stats");
            throw new GitHubFetchException(username, e);
        }
    }

    private int[] countStarsForks(String username, JsonNode firstPageUser) {
        int totalStars = 0;
        int totalForks = 0;
        String cursor = null;
        JsonNode user = firstPageUser;

        while (true) {
            for (JsonNode repo : user.path("repositories").path("nodes")) {
                totalStars += repo.path("stargazerCount").asInt();
                totalForks += repo.path("forkCount").asInt();
            }
            JsonNode pageInfo = user.path("repositories").path("pageInfo");
            if (!pageInfo.path("hasNextPage").asBoolean()) break;
            cursor = pageInfo.path("endCursor").asText();
            user = executeQuery(gitHubProperties.getQuery(), username, cursor).path("data").path("user");
        }

        return new int[]{totalStars, totalForks};
    }

    private JsonNode executeQuery(String query, String username, String cursor) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        if (cursor != null) variables.put("after", cursor);

        return restClient.post()
                .uri("/graphql")
                .body(Map.of("query", query, "variables", variables))
                .retrieve()
                .body(JsonNode.class);
    }

    private long parseAgeInDays(String createdAt) {
        LocalDate createdDate = LocalDate.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME);
        return ChronoUnit.DAYS.between(createdDate, LocalDate.now());
    }

    private int parseCommits(JsonNode contributions) {
        return contributions.path("totalCommitContributions").asInt()
                + contributions.path("restrictedContributionsCount").asInt();
    }

    private List<WeekActivity> parseHeatmap(JsonNode contributions) {
        List<WeekActivity> heatmap = new ArrayList<>();
        for (JsonNode week : contributions.path("contributionCalendar").path("weeks")) {
            LocalDate weekStart = LocalDate.parse(week.path("firstDay").asText());
            JsonNode days = week.path("contributionDays");
            int[] dayCounts = new int[days.size()];
            int total = 0;
            for (int i = 0; i < days.size(); i++) {
                int count = days.get(i).path("contributionCount").asInt();
                dayCounts[i] = count;
                total += count;
            }
            heatmap.add(new WeekActivity(weekStart, dayCounts, total));
        }
        return heatmap;
    }

    private GitHubRepo mapRepo(JsonNode node) {
        return new GitHubRepo(
                node.path("name").asText(),
                node.path("description").asText(null),
                node.path("url").asText(),
                node.path("stargazerCount").asInt(),
                node.path("forkCount").asInt()
        );
    }
}
