package ru.devanalyzer.analyzer_service.clients;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.devanalyzer.analyzer_service.config.GitHubProperties;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.dto.WeekActivity;

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

    public Long getGithubId(String username) {
        JsonNode user = restClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .body(JsonNode.class);
        return user.path("id").asLong();
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
            List<WeekActivity> heatmap = new ArrayList<>();

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

                    JsonNode weeks = contributions.path("contributionCalendar").path("weeks");
                    for (JsonNode week : weeks) {
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

            return new GitHubStats(githubId, repositories, totalStars, totalForks, followers, commits, ageInDays, heatmap);

        } catch (Exception e) {
            log.atError().addKeyValue("user", username).setCause(e).log("failed to fetch github stats");
            throw new RuntimeException("failed to get github stats: " + username);
        }
    }
}