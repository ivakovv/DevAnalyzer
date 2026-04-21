package ru.devanalyzer.analyzer_service.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TechnologyNameFormatterTest {

    @Test
    void formatTechnologies_shouldReturnEmptyList_whenInputIsNull() {
        List<String> result = TechnologyNameFormatter.formatTechnologies(null);

        assertThat(result).isEmpty();
    }

    @Test
    void formatTechnologies_shouldReturnEmptyList_whenInputIsEmpty() {
        List<String> result = TechnologyNameFormatter.formatTechnologies(List.of());

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("technologyNameProvider")
    void formatTechnologies_shouldFormatCorrectly(String input, String expected) {
        List<String> result = TechnologyNameFormatter.formatTechnologies(List.of(input));

        assertThat(result).containsExactly(expected);
    }

    static Stream<Arguments> technologyNameProvider() {
        return Stream.of(
                Arguments.of("java", "Java"),
                Arguments.of("JAVA", "Java"),
                Arguments.of("py", "Python"),
                Arguments.of("python", "Python"),
                Arguments.of("js", "JavaScript"),
                Arguments.of("javascript", "JavaScript"),
                Arguments.of("ts", "TypeScript"),
                Arguments.of("typescript", "TypeScript"),
                Arguments.of("xml", "XML"),
                Arguments.of("yaml", "YAML"),
                Arguments.of("yml", "YAML"),
                Arguments.of("json", "JSON"),
                Arguments.of("html", "HTML"),
                Arguments.of("css", "CSS"),
                Arguments.of("scss", "SCSS"),
                Arguments.of("sass", "Sass"),
                Arguments.of("docker", "Docker"),
                Arguments.of("dockerfile", "Docker"),
                Arguments.of("sql", "SQL"),
                Arguments.of("go", "Go"),
                Arguments.of("rust", "Rust"),
                Arguments.of("c", "C"),
                Arguments.of("cpp", "C++"),
                Arguments.of("c++", "C++"),
                Arguments.of("cs", "C#"),
                Arguments.of("csharp", "C#"),
                Arguments.of("php", "PHP"),
                Arguments.of("ruby", "Ruby"),
                Arguments.of("rb", "Ruby"),
                Arguments.of("swift", "Swift"),
                Arguments.of("kotlin", "Kotlin"),
                Arguments.of("kt", "Kotlin")
        );
    }

    @Test
    void formatTechnologies_shouldRemoveDuplicates() {
        List<String> input = List.of("java", "JAVA", "Java");

        List<String> result = TechnologyNameFormatter.formatTechnologies(input);

        assertThat(result).containsExactly("Java");
    }

    @Test
    void formatTechnologies_shouldSortAlphabetically() {
        List<String> input = List.of("python", "java", "go", "rust");

        List<String> result = TechnologyNameFormatter.formatTechnologies(input);

        assertThat(result).containsExactly("Go", "Java", "Python", "Rust");
    }

    @Test
    void formatTechnologies_shouldCapitalizeUnknownTechnologies() {
        List<String> input = List.of("unknownlang", "anotherone");

        List<String> result = TechnologyNameFormatter.formatTechnologies(input);

        assertThat(result).containsExactly("Anotherone", "Unknownlang");
    }

    @Test
    void formatFrameworks_shouldReturnEmptyList_whenInputIsNull() {
        List<String> result = TechnologyNameFormatter.formatFrameworks(null);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("frameworkNameProvider")
    void formatFrameworks_shouldFormatCorrectly(String input, String expected) {
        List<String> result = TechnologyNameFormatter.formatFrameworks(List.of(input));

        assertThat(result).containsExactly(expected);
    }

    static Stream<Arguments> frameworkNameProvider() {
        return Stream.of(
                Arguments.of("spring boot", "Spring"),
                Arguments.of("springboot", "Spring"),
                Arguments.of("spring", "Spring"),
                Arguments.of("postgresql", "PostgreSQL"),
                Arguments.of("postgres", "PostgreSQL"),
                Arguments.of("mysql", "MySQL"),
                Arguments.of("mongodb", "MongoDB"),
                Arguments.of("redis", "Redis"),
                Arguments.of("kafka", "Apache Kafka"),
                Arguments.of("apache kafka", "Apache Kafka"),
                Arguments.of("react", "React"),
                Arguments.of("reactjs", "React"),
                Arguments.of("vue", "Vue.js"),
                Arguments.of("vuejs", "Vue.js"),
                Arguments.of("angular", "Angular"),
                Arguments.of("django", "Django"),
                Arguments.of("flask", "Flask"),
                Arguments.of("fastapi", "FastAPI"),
                Arguments.of("express", "Express.js"),
                Arguments.of("expressjs", "Express.js"),
                Arguments.of("nestjs", "NestJS"),
                Arguments.of("nextjs", "Next.js")
        );
    }

    @Test
    void formatFrameworks_shouldRemoveDuplicatesAndSort() {
        List<String> input = List.of("spring", "react", "SPRING", "vue", "reactjs");

        List<String> result = TechnologyNameFormatter.formatFrameworks(input);

        assertThat(result).containsExactly("React", "Spring", "Vue.js");
    }

    @Test
    void formatFrameworks_shouldCapitalizeUnknownFrameworks() {
        List<String> input = List.of("unknownframework", "customlib");

        List<String> result = TechnologyNameFormatter.formatFrameworks(input);

        assertThat(result).containsExactly("Customlib", "Unknownframework");
    }

    @Test
    void mixedFormatting_shouldHandleEmptyStrings() {
        List<String> input = List.of("", "  ", "java");

        List<String> result = TechnologyNameFormatter.formatTechnologies(input);

        assertThat(result).containsExactly("", "  ", "Java");
    }
}
