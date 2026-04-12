package ru.devanalyzer.analyzer_service.util;

import java.util.List;
import java.util.Map;

public class TechnologyNameFormatter {

    private static final Map<String, String> TECHNOLOGY_NAMES = Map.ofEntries(
            Map.entry("java", "Java"),
            Map.entry("py", "Python"),
            Map.entry("python", "Python"),
            Map.entry("js", "JavaScript"),
            Map.entry("javascript", "JavaScript"),
            Map.entry("ts", "TypeScript"),
            Map.entry("typescript", "TypeScript"),
            Map.entry("xml", "XML"),
            Map.entry("yaml", "YAML"),
            Map.entry("yml", "YAML"),
            Map.entry("json", "JSON"),
            Map.entry("html", "HTML"),
            Map.entry("css", "CSS"),
            Map.entry("scss", "SCSS"),
            Map.entry("sass", "Sass"),
            Map.entry("docker", "Docker"),
            Map.entry("dockerfile", "Docker"),
            Map.entry("sql", "SQL"),
            Map.entry("web", "Web"),
            Map.entry("go", "Go"),
            Map.entry("rust", "Rust"),
            Map.entry("c", "C"),
            Map.entry("cpp", "C++"),
            Map.entry("c++", "C++"),
            Map.entry("cs", "C#"),
            Map.entry("csharp", "C#"),
            Map.entry("php", "PHP"),
            Map.entry("ruby", "Ruby"),
            Map.entry("rb", "Ruby"),
            Map.entry("swift", "Swift"),
            Map.entry("kotlin", "Kotlin"),
            Map.entry("kt", "Kotlin")
    );

    private static final Map<String, String> FRAMEWORK_NAMES = Map.ofEntries(
            Map.entry("spring boot", "Spring"),
            Map.entry("springboot", "Spring"),
            Map.entry("spring web", "Spring"),
            Map.entry("spring security", "Spring"),
            Map.entry("spring", "Spring"),
            Map.entry("lombok", "Lombok"),
            Map.entry("mapstruct", "MapStruct"),
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("postgres", "PostgreSQL"),
            Map.entry("mysql", "MySQL"),
            Map.entry("mongodb", "MongoDB"),
            Map.entry("redis", "Redis"),
            Map.entry("kafka", "Apache Kafka"),
            Map.entry("apache kafka", "Apache Kafka"),
            Map.entry("react", "React"),
            Map.entry("reactjs", "React"),
            Map.entry("vue", "Vue.js"),
            Map.entry("vuejs", "Vue.js"),
            Map.entry("angular", "Angular"),
            Map.entry("django", "Django"),
            Map.entry("flask", "Flask"),
            Map.entry("fastapi", "FastAPI"),
            Map.entry("express", "Express.js"),
            Map.entry("expressjs", "Express.js"),
            Map.entry("nestjs", "NestJS"),
            Map.entry("nextjs", "Next.js"),
            Map.entry("next.js", "Next.js")
    );

    public static List<String> formatTechnologies(List<String> technologies) {
        if (technologies == null) {
            return List.of();
        }
        
        return technologies.stream()
                .map(tech -> TECHNOLOGY_NAMES.getOrDefault(tech.toLowerCase(), capitalize(tech)))
                .distinct()
                .sorted()
                .toList();
    }

    public static List<String> formatFrameworks(List<String> frameworks) {
        if (frameworks == null) {
            return List.of();
        }
        
        return frameworks.stream()
                .map(fw -> FRAMEWORK_NAMES.getOrDefault(fw.toLowerCase(), capitalize(fw)))
                .distinct()
                .sorted()
                .toList();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
