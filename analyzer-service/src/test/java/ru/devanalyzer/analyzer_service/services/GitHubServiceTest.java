package ru.devanalyzer.analyzer_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.clients.GitHubClient;
import ru.devanalyzer.analyzer_service.dto.GitHubRepo;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;
import ru.devanalyzer.analyzer_service.dto.WeekActivity;
import ru.devanalyzer.analyzer_service.entity.CommitHeatmapEntity;
import ru.devanalyzer.analyzer_service.entity.GitHubStatsEntity;
import ru.devanalyzer.analyzer_service.repository.CommitHeatMapRepository;
import ru.devanalyzer.analyzer_service.repository.GitHubStatsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private GitHubStatsRepository gitHubStatsRepository;

    @Mock
    private CommitHeatMapRepository heatmapRepository;

    @InjectMocks
    private GitHubService gitHubService;

    @Captor
    private ArgumentCaptor<GitHubStatsEntity> statsEntityCaptor;

    @Captor
    private ArgumentCaptor<List<CommitHeatmapEntity>> heatmapEntitiesCaptor;

    private GitHubStats stats;
    private GitHubStatsEntity existingEntity;

    @BeforeEach
    void setUp() {
        List<WeekActivity> heatmap = List.of(
                new WeekActivity(LocalDate.of(2024, 1, 1), new int[]{5, 3, 7, 2, 0, 0, 0}, 17),
                new WeekActivity(LocalDate.of(2024, 1, 8), new int[]{8, 4, 6, 3, 1, 0, 0}, 22)
        );

        stats = new GitHubStats(
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
                heatmap
        );

        existingEntity = GitHubStatsEntity.builder()
                .id(1L)
                .githubId(12345L)
                .login("testuser")
                .name("Old Name")
                .location("Old Location")
                .company("Old Company")
                .repositories(20)
                .stars(100)
                .forks(20)
                .followers(80)
                .commits(400)
                .ageInDays(900L)
                .fetchedAt(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    void getRepositories_shouldReturnRepositoriesFromClient() {
        List<GitHubRepo> expectedRepos = List.of(
                new GitHubRepo("repo1", "Desc 1", "https://github.com/testuser/repo1", 10, 5),
                new GitHubRepo("repo2", "Desc 2", "https://github.com/testuser/repo2", 20, 8)
        );

        when(gitHubClient.getRepositories("testuser")).thenReturn(expectedRepos);

        List<GitHubRepo> result = gitHubService.getRepositories("testuser");

        assertThat(result).isEqualTo(expectedRepos);
        verify(gitHubClient).getRepositories("testuser");
    }

    @Test
    void getStats_shouldReturnCachedStats_whenFetchedToday() {
        GitHubStatsEntity todayEntity = GitHubStatsEntity.builder()
                .githubId(12345L)
                .login("testuser")
                .name("Test User")
                .location("Moscow")
                .company("Test Company")
                .repositories(25)
                .stars(150)
                .forks(30)
                .followers(100)
                .commits(500)
                .ageInDays(1000L)
                .fetchedAt(LocalDateTime.now())
                .build();

        List<CommitHeatmapEntity> heatmapEntities = List.of(
                CommitHeatmapEntity.builder()
                        .githubId(12345L)
                        .weekStart(LocalDate.of(2024, 1, 1))
                        .days(new int[]{5, 3, 7, 2, 0, 0, 0})
                        .total(17)
                        .build()
        );

        when(gitHubClient.getGitHubStats("testuser")).thenReturn(stats);
        when(gitHubStatsRepository.findByGithubId(12345L)).thenReturn(Optional.of(todayEntity));
        when(heatmapRepository.findByGithubIdOrderByWeekStartAsc(12345L)).thenReturn(heatmapEntities);

        GitHubStats result = gitHubService.getStats("testuser");

        assertThat(result.githubId()).isEqualTo(12345L);
        assertThat(result.login()).isEqualTo("testuser");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.heatmap()).hasSize(1);

        verify(gitHubStatsRepository, never()).save(any());
        verify(heatmapRepository, never()).deleteByGithubId(anyLong());
        verify(heatmapRepository, never()).saveAll(anyList());
    }

    @Test
    void getStats_shouldUpdateExistingEntity_whenNotFetchedToday() {
        when(gitHubClient.getGitHubStats("testuser")).thenReturn(stats);
        when(gitHubStatsRepository.findByGithubId(12345L)).thenReturn(Optional.of(existingEntity));

        GitHubStats result = gitHubService.getStats("testuser");

        verify(gitHubStatsRepository).save(statsEntityCaptor.capture());
        verify(heatmapRepository).deleteByGithubId(12345L);
        verify(heatmapRepository).saveAll(heatmapEntitiesCaptor.capture());

        GitHubStatsEntity savedEntity = statsEntityCaptor.getValue();
        assertThat(savedEntity.getGithubId()).isEqualTo(12345L);
        assertThat(savedEntity.getLogin()).isEqualTo("testuser");
        assertThat(savedEntity.getName()).isEqualTo("Test User");
        assertThat(savedEntity.getLocation()).isEqualTo("Moscow");
        assertThat(savedEntity.getCompany()).isEqualTo("Test Company");
        assertThat(savedEntity.getRepositories()).isEqualTo(25);
        assertThat(savedEntity.getStars()).isEqualTo(150);
        assertThat(savedEntity.getForks()).isEqualTo(30);
        assertThat(savedEntity.getFollowers()).isEqualTo(100);
        assertThat(savedEntity.getCommits()).isEqualTo(500);
        assertThat(savedEntity.getAgeInDays()).isEqualTo(1000L);

        List<CommitHeatmapEntity> savedHeatmap = heatmapEntitiesCaptor.getValue();
        assertThat(savedHeatmap).hasSize(2);
        assertThat(savedHeatmap.getFirst().getWeekStart()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(savedHeatmap.getFirst().getTotal()).isEqualTo(17);

        assertThat(result).isEqualTo(stats);
    }

    @Test
    void getStats_shouldCreateNewEntity_whenNotExists() {
        when(gitHubClient.getGitHubStats("testuser")).thenReturn(stats);
        when(gitHubStatsRepository.findByGithubId(12345L)).thenReturn(Optional.empty());

        GitHubStats result = gitHubService.getStats("testuser");

        verify(gitHubStatsRepository).save(statsEntityCaptor.capture());
        verify(heatmapRepository).deleteByGithubId(12345L);
        verify(heatmapRepository).saveAll(anyList());

        GitHubStatsEntity savedEntity = statsEntityCaptor.getValue();
        assertThat(savedEntity.getGithubId()).isEqualTo(12345L);
        assertThat(savedEntity.getLogin()).isEqualTo("testuser");
        assertThat(savedEntity.getFetchedAt()).isNotNull();

        assertThat(result).isEqualTo(stats);
    }

    @Test
    void getStats_shouldDeleteOldHeatmapBeforeSavingNew() {
        when(gitHubClient.getGitHubStats("testuser")).thenReturn(stats);
        when(gitHubStatsRepository.findByGithubId(12345L)).thenReturn(Optional.empty());

        gitHubService.getStats("testuser");

        verify(heatmapRepository).deleteByGithubId(12345L);
        verify(heatmapRepository).saveAll(anyList());
    }

    @Test
    void getRepositories_shouldReturnEmptyList_whenNoRepositories() {
        when(gitHubClient.getRepositories("testuser")).thenReturn(List.of());

        List<GitHubRepo> result = gitHubService.getRepositories("testuser");

        assertThat(result).isEmpty();
    }
}
