package ru.devanalyzer.analyzer_service.services.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.impl.ForkFilter;
import ru.devanalyzer.analyzer_service.filter.impl.SizeFilter;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryFilterServiceTest {

    @Mock
    private ForkFilter forkFilter;

    @Mock
    private SizeFilter sizeFilter;

    private RepositoryFilterService filterService;
    private List<GitHubRepository> repositories;

    @BeforeEach
    void setUp() {
        filterService = new RepositoryFilterService(List.of(forkFilter, sizeFilter));

        repositories = List.of(
                createRepository("repo1", "Java", false, 1024),
                createRepository("repo2", "Python", true, 2048),
                createRepository("repo3", "Go", false, 100),
                createRepository("repo4", "Rust", false, 5120),
                createRepository("repo5", "JavaScript", false, 2048)
        );
    }

    @Test
    void filterRepositories_shouldApplyAllFilters() {
        when(forkFilter.test(any())).thenAnswer(inv -> {
            GitHubRepository repo = inv.getArgument(0);
            return !repo.isFork();
        });
        lenient().when(forkFilter.getRejectionReason()).thenReturn("Is fork");

        when(sizeFilter.test(any())).thenAnswer(inv -> {
            GitHubRepository repo = inv.getArgument(0);
            return repo.size() >= 500 && repo.size() <= 3000;
        });
        lenient().when(sizeFilter.getRejectionReason()).thenReturn("Invalid size");

        List<GitHubRepository> result = filterService.filterRepositories(repositories, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GitHubRepository::name)
                .containsExactly("repo1", "repo5");
    }

    @Test
    void filterRepositories_shouldIncludeLanguageFilterWhenLanguagesProvided() {
        when(forkFilter.test(any())).thenReturn(true);
        when(sizeFilter.test(any())).thenReturn(true);

        List<String> requiredLanguages = List.of("Java", "Python");

        List<GitHubRepository> result = filterService.filterRepositories(repositories, requiredLanguages);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GitHubRepository::name)
                .containsExactly("repo1", "repo2");
    }

    @Test
    void filterRepositories_shouldReturnEmptyListWhenNoRepositoriesPass() {
        when(forkFilter.test(any())).thenReturn(false);
        lenient().when(forkFilter.getRejectionReason()).thenReturn("Is fork");

        List<GitHubRepository> result = filterService.filterRepositories(repositories, null);

        assertThat(result).isEmpty();
    }

    @Test
    void filterRepositories_shouldReturnAllWhenAllPass() {
        when(forkFilter.test(any())).thenReturn(true);
        when(sizeFilter.test(any())).thenReturn(true);

        List<GitHubRepository> result = filterService.filterRepositories(repositories, null);

        assertThat(result).hasSize(5);
    }

    @Test
    void filterRepositories_shouldHandleEmptyRepositoryList() {
        List<GitHubRepository> result = filterService.filterRepositories(List.of(), null);

        assertThat(result).isEmpty();
    }

    @Test
    void filterRepositories_shouldHandleNullLanguages() {
        when(forkFilter.test(any())).thenReturn(true);
        when(sizeFilter.test(any())).thenReturn(true);

        List<GitHubRepository> result = filterService.filterRepositories(repositories, null);

        assertThat(result).hasSize(5);
    }

    @Test
    void filterRepositories_shouldBeCaseInsensitiveForLanguages() {
        when(forkFilter.test(any())).thenReturn(true);
        when(sizeFilter.test(any())).thenReturn(true);

        List<String> requiredLanguages = List.of("JAVA", "python");

        List<GitHubRepository> result = filterService.filterRepositories(repositories, requiredLanguages);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GitHubRepository::name)
                .containsExactly("repo1", "repo2");
    }

    @Test
    void filterRepositories_shouldRejectRepositoriesWithNoLanguages() {
        GitHubRepository repoNoLang = new GitHubRepository(
                "repo-nolang", "user/repo-nolang",
                "https://github.com/user/repo-nolang.git",
                new GitHubRepository.Owner("user"),
                false, 1024, 10, 5, null, null,
                Instant.now(), Instant.now(),
                "Test", true, 3, "main", 100, List.of()
        );

        when(forkFilter.test(any())).thenReturn(true);
        when(sizeFilter.test(any())).thenReturn(true);

        List<GitHubRepository> repos = List.of(repoNoLang);
        List<GitHubRepository> result = filterService.filterRepositories(repos, List.of("Java"));

        assertThat(result).isEmpty();
    }

    private GitHubRepository createRepository(String name, String language, boolean isFork, int size) {
        return new GitHubRepository(
                name,
                "user/" + name,
                "https://github.com/user/" + name + ".git",
                new GitHubRepository.Owner("user"),
                isFork,
                size,
                10,
                5,
                language,
                language != null ? List.of(language) : List.of(),
                Instant.now(),
                Instant.now().minus(365, java.time.temporal.ChronoUnit.DAYS),
                "Test repo " + name,
                true,
                3,
                "main",
                100,
                List.of()
        );
    }
}
