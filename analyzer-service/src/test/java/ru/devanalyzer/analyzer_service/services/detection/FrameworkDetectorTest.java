package ru.devanalyzer.analyzer_service.services.detection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkDetectorTest {

    private FrameworkDetector detector;

    @BeforeEach
    void setUp() {
        detector = new FrameworkDetector();
    }

    @Test
    void detectFrameworks_shouldReturnEmptyList_whenNoTopicsAndNoFiles() {
        List<String> result = detector.detectFrameworks(null, "/nonexistent/path");

        assertThat(result).isEmpty();
    }

    @Test
    void detectFrameworks_shouldNormalizeGitHubTopics() {
        List<String> topics = List.of("react", "spring-boot", "docker", "kubernetes");

        List<String> result = detector.detectFrameworks(topics, "/nonexistent/path");

        assertThat(result).containsExactlyInAnyOrder(
                "React", "Spring Boot", "Docker", "Kubernetes"
        );
    }

    @Test
    void detectFrameworks_shouldCapitalizeUnknownTopics() {
        List<String> topics = List.of("unknown-framework", "custom-tool");

        List<String> result = detector.detectFrameworks(topics, "/nonexistent/path");

        assertThat(result).containsExactlyInAnyOrder(
                "Unknown Framework", "Custom Tool"
        );
    }

    @Test
    void detectFrameworks_shouldDetectFromPomXml(@TempDir Path tempDir) throws IOException {
        String pomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-jpa</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.postgresql</groupId>
                            <artifactId>postgresql</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;

        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        // FrameworkDetector находит "spring-boot" внутри artifactId и маппит на "Spring Boot"
        // "postgresql" на "PostgreSQL", "lombok" на "Lombok"
        assertThat(result).contains(
                "Spring Boot",
                "PostgreSQL",
                "Lombok"
        );
    }

    @Test
    void detectFrameworks_shouldDetectFromPackageJson(@TempDir Path tempDir) throws IOException {
        String packageJsonContent = """
                {
                    "name": "test-app",
                    "dependencies": {
                        "react": "^18.0.0",
                        "express": "^4.18.0",
                        "mongoose": "^6.0.0",
                        "typescript": "^5.0.0",
                        "tailwindcss": "^3.0.0"
                    },
                    "devDependencies": {
                        "vite": "^4.0.0",
                        "@types/node": "^20.0.0"
                    }
                }
                """;

        Files.writeString(tempDir.resolve("package.json"), packageJsonContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).contains(
                "React",
                "Express.js",
                "MongoDB",
                "TypeScript",
                "Tailwind CSS",
                "Vite"
        );
    }

    @Test
    void detectFrameworks_shouldDetectFromRequirementsTxt(@TempDir Path tempDir) throws IOException {
        String requirementsContent = """
                django==4.2.0
                flask==2.3.0
                fastapi==0.100.0
                pandas==2.0.0
                numpy==1.24.0
                tensorflow==2.13.0
                # This is a comment
                requests==2.31.0
                """;

        Files.writeString(tempDir.resolve("requirements.txt"), requirementsContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).contains(
                "Django",
                "Flask",
                "FastAPI",
                "Pandas",
                "NumPy",
                "TensorFlow"
        );
    }

    @Test
    void detectFrameworks_shouldCombineTopicsAndFileDetection(@TempDir Path tempDir) throws IOException {
        List<String> topics = List.of("kubernetes", "aws");

        String packageJsonContent = """
                {
                    "dependencies": {
                        "react": "^18.0.0",
                        "express": "^4.18.0"
                    }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJsonContent);

        List<String> result = detector.detectFrameworks(topics, tempDir.toString());

        assertThat(result).contains(
                "Kubernetes",
                "AWS",
                "React",
                "Express.js"
        );
    }

    @Test
    void detectFrameworks_shouldRemoveDuplicates() {
        List<String> topics = List.of("react", "React", "REACT");

        List<String> result = detector.detectFrameworks(topics, "/nonexistent/path");

        assertThat(result).containsExactly("React");
    }

    @Test
    void detectFrameworks_shouldHandleEmptyTopics() {
        List<String> result = detector.detectFrameworks(List.of(), "/nonexistent/path");

        assertThat(result).isEmpty();
    }

    @Test
    void detectFrameworks_shouldHandleMalformedFiles(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("pom.xml"), "not valid xml {");

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).isEmpty();
    }

    @Test
    void detectFrameworks_shouldNormalizeTopicsWithDashes() {
        List<String> result = detector.detectFrameworks(
                List.of("spring-cloud-aws", "react-router"),
                "/nonexistent/path"
        );

        // нормализует через capitalizeWords
        assertThat(result).contains("Spring Cloud Aws", "React Router");
    }

    @Test
    void detectFrameworks_shouldCapitalizeUnknownArtifactId() {
        // Для неизвестных артефактов FrameworkDetector просто капитализирует слова
        List<String> result = detector.detectFrameworks(
                List.of("spring-boot-starter-web"),
                "/nonexistent/path"
        );

        assertThat(result).contains("Spring Boot Starter Web");
    }

    @Test
    void detectFrameworks_shouldHandleMultipleFileTypes(@TempDir Path tempDir) throws IOException {
        String pomContent = """
                <project>
                    <dependencies>
                        <dependency><artifactId>spring-boot</artifactId></dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        String packageJsonContent = """
                {"dependencies": {"vue": "^3.0.0"}}
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJsonContent);

        String requirementsContent = "django==4.2.0";
        Files.writeString(tempDir.resolve("requirements.txt"), requirementsContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).contains("Spring Boot", "Vue.js", "Django");
    }

    @Test
    void detectFrameworks_shouldDetectNestJSFromPackageJson(@TempDir Path tempDir) throws IOException {
        String packageJsonContent = """
                {
                    "dependencies": {
                        "@nestjs/core": "^10.0.0",
                        "@nestjs/common": "^10.0.0"
                    }
                }
                """;

        Files.writeString(tempDir.resolve("package.json"), packageJsonContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).contains("NestJS");
    }

    @Test
    void detectFrameworks_shouldDetectGraphQLFromPackageJson(@TempDir Path tempDir) throws IOException {
        String packageJsonContent = """
                {
                    "dependencies": {
                        "@apollo/server": "^4.0.0",
                        "graphql": "^16.0.0"
                    }
                }
                """;

        Files.writeString(tempDir.resolve("package.json"), packageJsonContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).contains("GraphQL");
    }

    @Test
    void detectFrameworks_shouldDetectHibernateFromPomXml(@TempDir Path tempDir) throws IOException {
        String pomContent = """
                <project>
                    <dependencies>
                        <dependency>
                            <artifactId>hibernate-core</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """;

        Files.writeString(tempDir.resolve("pom.xml"), pomContent);

        List<String> result = detector.detectFrameworks(null, tempDir.toString());

        assertThat(result).contains("Hibernate");
    }
}
