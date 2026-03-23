package ru.devanalyzer.analyzer_service.clients;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class GitHubClient {

    private final RestClient restClient;

    public GitHubClient(@Value("${app.github.token}") String token) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "token " + token)
                .build();
    }

    public Integer getAmountOfCommitsByUser(String user) {
        log.atInfo().addKeyValue("user", user).log("Fetch total amount of commits of user");
        return restClient.get()
                .uri("/search/commits?q=author:{username}", user)
                .accept(MediaType.parseMediaType("application/vnd.github.cloak-preview+json"))
                .retrieve()
                .body(JsonNode.class)
                .path("total_count")
                .asInt();
    }


    public Integer getAmountOfRepositoriesByUser(String user) {
        log.atInfo().addKeyValue("user", user).log("Fetch amount of user repositories");
        JsonNode repos = restClient.get()
                .uri("/users/{username}/repos", user)
                .retrieve()
                .body(JsonNode.class);
        return repos.size();
    }

    
}