package ru.devanalyzer.analyzer_service.services.sonar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.analyzer_service.config.properties.SonarQubeProperties;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SonarScannerExecutorTest {

    @Mock
    private SonarQubeProperties sonarQubeProperties;

    @InjectMocks
    private SonarScannerExecutor executor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        when(sonarQubeProperties.getHostUrl()).thenReturn("http://localhost:9000");
        when(sonarQubeProperties.getToken()).thenReturn("test-token");
    }

    @Test
    void executeScan_shouldThrowException_whenDirectoryDoesNotExist() {
        assertThatThrownBy(() -> executor.executeScan(
                "test-project",
                "Test Project",
                "/nonexistent/directory"
        )).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SonarQube scan execution failed");
    }

    @Test
    void executeScan_shouldHandleEmptyToken() {
        when(sonarQubeProperties.getToken()).thenReturn("");

        assertThatThrownBy(() -> executor.executeScan(
                "test-project",
                "Test Project",
                tempDir.toString()
        )).isInstanceOf(RuntimeException.class);
    }

    @Test
    void executeScan_shouldHandleNullToken() {
        when(sonarQubeProperties.getToken()).thenReturn(null);

        assertThatThrownBy(() -> executor.executeScan(
                "test-project",
                "Test Project",
                tempDir.toString()
        )).isInstanceOf(RuntimeException.class);
    }

    // Note: Real SonarScanner tests would require SonarQube server running.
    // These are commented out as they would be integration tests.
    /*
    @Test
    void executeScan_shouldRunSuccessfully_whenValidDirectory() {
        assertThatCode(() -> executor.executeScan(
                "test-project",
                "Test Project",
                tempDir.toString()
        )).doesNotThrowAnyException();
    }
    */
}
