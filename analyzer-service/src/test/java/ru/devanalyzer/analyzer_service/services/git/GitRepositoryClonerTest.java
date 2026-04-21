package ru.devanalyzer.analyzer_service.services.git;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.config.properties.ScanProperties;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitRepositoryClonerTest {

    @Mock
    private ScanProperties scanProperties;

    @InjectMocks
    private GitRepositoryCloner cloner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        when(scanProperties.getCloneTimeoutMinutes()).thenReturn(5);
    }

    @Test
    void cloneRepository_shouldThrowException_whenRepositoryDoesNotExist() {
        String nonExistentUrl = "https://github.com/nonexistent-user/nonexistent-repo.git";
        String targetDir = tempDir.resolve("nonexistent").toString();

        assertThatThrownBy(() -> cloner.cloneRepository(nonExistentUrl, targetDir))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Clone failed after 3 attempts");
    }

    @Test
    void cloneRepository_shouldThrowException_whenInvalidUrl() {
        String invalidUrl = "not-a-valid-url";
        String targetDir = tempDir.resolve("invalid").toString();

        assertThatThrownBy(() -> cloner.cloneRepository(invalidUrl, targetDir))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void cloneRepository_shouldThrowException_whenTargetDirectoryIsFile() throws Exception {
        Path filePath = tempDir.resolve("file.txt");
        Files.writeString(filePath, "content");

        assertThatThrownBy(() -> cloner.cloneRepository(
                "https://github.com/spring-projects/spring-boot.git",
                filePath.toString()))
                .isInstanceOf(RuntimeException.class);
    }

    // Note: Real clone tests would require network access and a real repository.
    // These are commented out as they would be integration tests.
    /*
    @Test
    void cloneRepository_shouldCloneSuccessfully_whenValidPublicRepository() {
        String validUrl = "https://github.com/octocat/Hello-World.git";
        String targetDir = tempDir.resolve("hello-world").toString();

        assertThatCode(() -> cloner.cloneRepository(validUrl, targetDir))
                .doesNotThrowAnyException();

        assertThat(Files.exists(Path.of(targetDir))).isTrue();
        assertThat(Files.exists(Path.of(targetDir, "README"))).isTrue();
    }
    */
}
