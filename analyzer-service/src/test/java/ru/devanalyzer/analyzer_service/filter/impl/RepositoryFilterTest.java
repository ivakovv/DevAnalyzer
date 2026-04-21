package ru.devanalyzer.analyzer_service.filter.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryFilterTest {

    private GitHubRepository createRepositoryWithFork(boolean isFork) {
        return new GitHubRepository(
                "test-repo",
                "user/test-repo",
                "https://github.com/user/test-repo.git",
                new GitHubRepository.Owner("user"),
                isFork,
                1024,
                10,
                5,
                "Java",
                List.of("Java", "Kotlin"),
                Instant.now(),
                Instant.now().minus(365, ChronoUnit.DAYS),
                "Test repository",
                true,
                3,
                "main",
                100,
                List.of()
        );
    }

    private GitHubRepository createRepositoryWithPushedAt(Instant pushedAt) {
        return new GitHubRepository(
                "test-repo",
                "user/test-repo",
                "https://github.com/user/test-repo.git",
                new GitHubRepository.Owner("user"),
                false,
                1024,
                10,
                5,
                "Java",
                List.of("Java", "Kotlin"),
                pushedAt,
                Instant.now().minus(365, ChronoUnit.DAYS),
                "Test repository",
                true,
                3,
                "main",
                100,
                List.of()
        );
    }

    private GitHubRepository createRepositoryWithSize(int size) {
        return new GitHubRepository(
                "test-repo",
                "user/test-repo",
                "https://github.com/user/test-repo.git",
                new GitHubRepository.Owner("user"),
                false,
                size,
                10,
                5,
                "Java",
                List.of("Java", "Kotlin"),
                Instant.now(),
                Instant.now().minus(365, ChronoUnit.DAYS),
                "Test repository",
                true,
                3,
                "main",
                100,
                List.of()
        );
    }

    private GitHubRepository createRepositoryWithLanguages(List<String> languages) {
        return new GitHubRepository(
                "test-repo",
                "user/test-repo",
                "https://github.com/user/test-repo.git",
                new GitHubRepository.Owner("user"),
                false,
                1024,
                10,
                5,
                languages != null && !languages.isEmpty() ? languages.getFirst() : null,
                languages,
                Instant.now(),
                Instant.now().minus(365, ChronoUnit.DAYS),
                "Test repository",
                true,
                3,
                "main",
                100,
                List.of()
        );
    }

    @Test
    void forkFilter_shouldRejectFork() {
        ForkFilter filter = new ForkFilter();
        GitHubRepository repo = createRepositoryWithFork(true);

        assertThat(filter.test(repo)).isFalse();
        assertThat(filter.getRejectionReason()).contains("fork");
    }

    @Test
    void forkFilter_shouldAcceptNonFork() {
        ForkFilter filter = new ForkFilter();
        GitHubRepository repo = createRepositoryWithFork(false);

        assertThat(filter.test(repo)).isTrue();
    }

    @Test
    void activityFilter_shouldRejectInactiveRepository() {
        ActivityFilter filter = new ActivityFilter();
        ReflectionTestUtils.setField(filter, "maxInactivityYears", 2);

        GitHubRepository repo = createRepositoryWithPushedAt(
                Instant.now().minus(3 * 365, ChronoUnit.DAYS)
        );

        assertThat(filter.test(repo)).isFalse();
        assertThat(filter.getRejectionReason()).contains("inactive");
    }

    @Test
    void activityFilter_shouldAcceptActiveRepository() {
        ActivityFilter filter = new ActivityFilter();
        ReflectionTestUtils.setField(filter, "maxInactivityYears", 2);

        GitHubRepository repo = createRepositoryWithPushedAt(
                Instant.now().minus(365, ChronoUnit.DAYS)
        );

        assertThat(filter.test(repo)).isTrue();
    }

    @Test
    void activityFilter_shouldRejectWhenPushedAtIsNull() {
        ActivityFilter filter = new ActivityFilter();
        GitHubRepository repo = createRepositoryWithPushedAt(null);

        assertThat(filter.test(repo)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "500, 5, true",
            "100, 5, false",
            "5000, 5, true",
            "1024, 10, true"
    })
    void sizeFilter_shouldValidateCorrectly(int sizeKb, int maxSizeMb, boolean expected) {
        SizeFilter filter = new SizeFilter();
        ReflectionTestUtils.setField(filter, "minSizeKb", 500);
        ReflectionTestUtils.setField(filter, "maxSizeMb", maxSizeMb);

        GitHubRepository repo = createRepositoryWithSize(sizeKb);

        assertThat(filter.test(repo)).isEqualTo(expected);
    }

    @Test
    void languageFilter_shouldAcceptWhenLanguageMatches() {
        LanguageFilter filter = new LanguageFilter(List.of("Java", "Python"));
        GitHubRepository repo = createRepositoryWithLanguages(List.of("Java", "Kotlin"));

        assertThat(filter.test(repo)).isTrue();
    }

    @Test
    void languageFilter_shouldRejectWhenNoLanguageMatches() {
        LanguageFilter filter = new LanguageFilter(List.of("Python", "Go"));
        GitHubRepository repo = createRepositoryWithLanguages(List.of("Java", "Kotlin"));

        assertThat(filter.test(repo)).isFalse();
        assertThat(filter.getRejectionReason()).contains("not in required languages");
    }

    @Test
    void languageFilter_shouldRejectWhenRepositoryHasNoLanguages() {
        LanguageFilter filter = new LanguageFilter(List.of("Java"));
        GitHubRepository repo = createRepositoryWithLanguages(null);

        assertThat(filter.test(repo)).isFalse();
        // Исправлено: реальное сообщение содержит список требуемых языков
        assertThat(filter.getRejectionReason()).contains("not in required languages");
    }

    @Test
    void languageFilter_shouldAcceptAnyLanguage_whenNoRequiredLanguages() {
        LanguageFilter filter = new LanguageFilter(null);
        GitHubRepository repo = createRepositoryWithLanguages(List.of("Java"));

        assertThat(filter.test(repo)).isTrue();
    }

    @Test
    void languageFilter_shouldBeCaseInsensitive() {
        LanguageFilter filter = new LanguageFilter(List.of("JAVA", "python"));
        GitHubRepository repo = createRepositoryWithLanguages(List.of("java", "Python"));

        assertThat(filter.test(repo)).isTrue();
    }

    @Test
    void languageFilter_shouldAcceptWhenEmptyRequiredLanguages() {
        LanguageFilter filter = new LanguageFilter(List.of());
        GitHubRepository repo = createRepositoryWithLanguages(List.of("Java"));

        assertThat(filter.test(repo)).isTrue();
    }

    @Test
    void languageFilter_shouldRejectWhenRepositoryLanguagesEmpty() {
        LanguageFilter filter = new LanguageFilter(List.of("Java"));
        GitHubRepository repo = createRepositoryWithLanguages(List.of());

        assertThat(filter.test(repo)).isFalse();

        assertThat(filter.getRejectionReason()).isEqualTo("Repository languages not in required languages: [Java]");
    }

    @Test
    void sizeFilter_shouldProvideCorrectRejectionReason() {
        SizeFilter filter = new SizeFilter();
        ReflectionTestUtils.setField(filter, "minSizeKb", 500);
        ReflectionTestUtils.setField(filter, "maxSizeMb", 5);

        assertThat(filter.getRejectionReason())
                .contains("500 KB")
                .contains("5 MB");
    }

    @Test
    void activityFilter_shouldProvideCorrectRejectionReason() {
        ActivityFilter filter = new ActivityFilter();
        ReflectionTestUtils.setField(filter, "maxInactivityYears", 3);

        assertThat(filter.getRejectionReason())
                .contains("3 years");
    }
}
