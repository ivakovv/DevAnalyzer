package ru.devanalyzer.analyzer_service.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.services.GitHubService;

@RestController
@RequestMapping("/api/github")
@AllArgsConstructor
public class GitHubStatsController {

    private final GitHubService gitHubService;

    @GetMapping("/{username}")
    public ResponseEntity<GitHubStats> getStats(@PathVariable String username) {
        return ResponseEntity.ok(gitHubService.getStats(username));
    }
}
