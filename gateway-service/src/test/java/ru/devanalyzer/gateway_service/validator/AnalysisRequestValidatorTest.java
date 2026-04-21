package ru.devanalyzer.gateway_service.validator;

import org.junit.jupiter.api.Test;
import ru.devanalyzer.gateway_service.exception.InvalidAnalysisRequestException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalysisRequestValidatorTest {

    @Test
    void validate_Success() {

        String githubUsername = "testuser";
        List<String> techStack = List.of("Java", "Spring");


        assertThatCode(() -> AnalysisRequestValidator.validate(githubUsername, techStack))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_EmptyGithubUsername_ThrowsException() {

        String githubUsername = "";
        List<String> techStack = List.of("Java");


        assertThatThrownBy(() -> AnalysisRequestValidator.validate(githubUsername, techStack))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("GitHub username is required");
    }

    @Test
    void validate_NullGithubUsername_ThrowsException() {

        String githubUsername = null;
        List<String> techStack = List.of("Java");


        assertThatThrownBy(() -> AnalysisRequestValidator.validate(githubUsername, techStack))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("GitHub username is required");
    }

    @Test
    void validate_BlankGithubUsername_ThrowsException() {

        String githubUsername = "   ";
        List<String> techStack = List.of("Java");


        assertThatThrownBy(() -> AnalysisRequestValidator.validate(githubUsername, techStack))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("GitHub username is required");
    }

    @Test
    void validate_EmptyTechStack_ThrowsException() {

        String githubUsername = "testuser";
        List<String> techStack = List.of();


        assertThatThrownBy(() -> AnalysisRequestValidator.validate(githubUsername, techStack))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("Tech stack is required for analysis");
    }

    @Test
    void validate_NullTechStack_ThrowsException() {

        String githubUsername = "testuser";
        List<String> techStack = null;


        assertThatThrownBy(() -> AnalysisRequestValidator.validate(githubUsername, techStack))
                .isInstanceOf(InvalidAnalysisRequestException.class)
                .hasMessage("Tech stack is required for analysis");
    }

    @Test
    void constructor_ThrowsUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            try {
                var constructor = AnalysisRequestValidator.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            } catch (Exception e) {
                throw e.getCause();
            }
        }).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Utility class");
    }
}
