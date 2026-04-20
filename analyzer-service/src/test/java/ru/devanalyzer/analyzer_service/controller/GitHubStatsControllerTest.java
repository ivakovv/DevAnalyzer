package ru.devanalyzer.analyzer_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devanalyzer.analyzer_service.dto.GitHubRepo;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.dto.WeekActivity;
import ru.devanalyzer.analyzer_service.exceptions.GitHubUserNotFoundException;
import ru.devanalyzer.analyzer_service.services.GitHubService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GitHubStatsController.class)
class GitHubStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private GitHubService gitHubService;

    @Test
    void getStats_shouldReturnGitHubStats_whenUserExists() throws Exception {
        GitHubStats stats = new GitHubStats(
                12345L,
                "testuser",
                "Test User",
                "Moscow",
                "Test Company",
                25,
                150,
                30,
                100,
                500,
                1000L,
                List.of(new WeekActivity(LocalDate.now(), new int[]{1, 2, 3}, 6))
        );

        when(gitHubService.getStats("testuser")).thenReturn(stats);

        mockMvc.perform(get("/api/github/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.githubId").value(12345))
                .andExpect(jsonPath("$.login").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.location").value("Moscow"))
                .andExpect(jsonPath("$.company").value("Test Company"))
                .andExpect(jsonPath("$.repositories").value(25))
                .andExpect(jsonPath("$.stars").value(150))
                .andExpect(jsonPath("$.forks").value(30))
                .andExpect(jsonPath("$.followers").value(100))
                .andExpect(jsonPath("$.commits").value(500))
                .andExpect(jsonPath("$.ageInDays").value(1000))
                .andExpect(jsonPath("$.heatmap").isArray())
                .andExpect(jsonPath("$.heatmap[0].total").value(6));
    }

    @Test
    void getStats_shouldReturn404_whenUserNotFound() throws Exception {
        when(gitHubService.getStats("nonexistent"))
                .thenThrow(new GitHubUserNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/github/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStats_shouldReturn500_whenServiceFails() throws Exception {
        when(gitHubService.getStats("testuser"))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/api/github/testuser"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRepositories_shouldReturnRepositories_whenUserExists() throws Exception {
        List<GitHubRepo> repos = List.of(
                new GitHubRepo("repo1", "Description 1", "https://github.com/testuser/repo1", 10, 5),
                new GitHubRepo("repo2", "Description 2", "https://github.com/testuser/repo2", 20, 8)
        );

        when(gitHubService.getRepositories("testuser")).thenReturn(repos);

        mockMvc.perform(get("/api/github/testuser/repos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("repo1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[0].url").value("https://github.com/testuser/repo1"))
                .andExpect(jsonPath("$[0].stars").value(10))
                .andExpect(jsonPath("$[0].forks").value(5))
                .andExpect(jsonPath("$[1].name").value("repo2"));
    }

    @Test
    void getRepositories_shouldReturn404_whenUserNotFound() throws Exception {
        when(gitHubService.getRepositories("nonexistent"))
                .thenThrow(new GitHubUserNotFoundException("nonexistent"));

        mockMvc.perform(get("/api/github/nonexistent/repos"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRepositories_shouldReturnEmptyList_whenNoRepositories() throws Exception {
        when(gitHubService.getRepositories("testuser")).thenReturn(List.of());

        mockMvc.perform(get("/api/github/testuser/repos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
