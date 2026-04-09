package ru.devanalyzer.analyzer_service.services.detection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class FrameworkDetector {

    private static final Pattern MAVEN_DEPENDENCY = Pattern.compile("<artifactId>([^<]+)</artifactId>");
    private static final Pattern NPM_DEPENDENCY = Pattern.compile("\"([^\"]+)\"\\s*:");
    private static final Pattern PYTHON_DEPENDENCY = Pattern.compile("^([a-zA-Z0-9_-]+)");
    
    private static final Map<String, String> FRAMEWORK_MAPPING = Map.ofEntries(
            // Java/Spring
            Map.entry("spring-boot", "Spring Boot"),
            Map.entry("spring-cloud", "Spring Cloud"),
            Map.entry("spring-security", "Spring Security"),
            Map.entry("spring-data", "Spring Data"),
            Map.entry("spring-web", "Spring Web"),
            Map.entry("spring-webmvc", "Spring MVC"),
            Map.entry("hibernate", "Hibernate"),
            Map.entry("kafka", "Apache Kafka"),
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("mysql", "MySQL"),
            Map.entry("mongodb", "MongoDB"),
            Map.entry("redis", "Redis"),
            Map.entry("lombok", "Lombok"),
            Map.entry("mapstruct", "MapStruct"),
            Map.entry("junit", "JUnit"),
            Map.entry("mockito", "Mockito"),
            
            // JavaScript/TypeScript
            Map.entry("react", "React"),
            Map.entry("vue", "Vue.js"),
            Map.entry("@angular/core", "Angular"),
            Map.entry("angular", "Angular"),
            Map.entry("next", "Next.js"),
            Map.entry("express", "Express.js"),
            Map.entry("typescript", "TypeScript"),
            Map.entry("webpack", "Webpack"),
            Map.entry("vite", "Vite"),
            Map.entry("tailwindcss", "Tailwind CSS"),
            Map.entry("redux", "Redux"),
            
            // Python
            Map.entry("django", "Django"),
            Map.entry("flask", "Flask"),
            Map.entry("fastapi", "FastAPI"),
            Map.entry("pandas", "Pandas"),
            Map.entry("numpy", "NumPy"),
            Map.entry("tensorflow", "TensorFlow"),
            Map.entry("pytorch", "PyTorch")
    );


    public List<String> detectFrameworks(List<String> githubTopics, String projectPath) {
        Set<String> frameworks = new LinkedHashSet<>();
        
        if (githubTopics != null && !githubTopics.isEmpty()) {
            frameworks.addAll(normalizeTopics(githubTopics));
            log.debug("Added {} frameworks from GitHub topics", githubTopics.size());
        }
        
        Set<String> detectedFromFiles = detectFromFiles(projectPath);
        frameworks.addAll(detectedFromFiles);
        log.debug("Detected {} frameworks from project files", detectedFromFiles.size());
        
        List<String> result = new ArrayList<>(frameworks);
        log.info("Total frameworks detected: {} for path: {}", result.size(), projectPath);
        return result;
    }
    

    private Set<String> normalizeTopics(List<String> topics) {
        Set<String> normalized = new LinkedHashSet<>();
        
        for (String topic : topics) {
            String mapped = FRAMEWORK_MAPPING.get(topic.toLowerCase());
            if (mapped != null) {
                normalized.add(mapped);
            } else {
                normalized.add(capitalizeWords(topic));
            }
        }
        
        return normalized;
    }
    
    private String capitalizeWords(String str) {
        String[] words = str.split("[-_\\s]+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
    

    private Set<String> detectFromFiles(String projectPath) {
        Set<String> frameworks = new HashSet<>();
        Path path = Path.of(projectPath);
        
        try {
            // Java
            if (Files.exists(path.resolve("pom.xml"))) {
                frameworks.addAll(detectFromMaven(path));
            }
            
            // JavaScript/TypeScript
            if (Files.exists(path.resolve("package.json"))) {
                frameworks.addAll(detectFromNpm(path));
            }
            
            // Python
            if (Files.exists(path.resolve("requirements.txt"))) {
                frameworks.addAll(detectFromPython(path));
            }
            
        } catch (Exception e) {
            log.warn("Error detecting frameworks from files: {}", projectPath, e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromMaven(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Path pomPath = projectPath.resolve("pom.xml");
            String content = Files.readString(pomPath);
            
            Matcher matcher = MAVEN_DEPENDENCY.matcher(content);
            while (matcher.find()) {
                String artifactId = matcher.group(1);
                String framework = findFrameworkByArtifact(artifactId);
                if (framework != null) {
                    frameworks.add(framework);
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read pom.xml", e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromNpm(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Path packageJson = projectPath.resolve("package.json");
            String content = Files.readString(packageJson);
            
            Matcher matcher = NPM_DEPENDENCY.matcher(content);
            while (matcher.find()) {
                String packageName = matcher.group(1);
                String framework = findFrameworkByArtifact(packageName);
                if (framework != null) {
                    frameworks.add(framework);
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read package.json", e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromPython(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Path reqPath = projectPath.resolve("requirements.txt");
            List<String> lines = Files.readAllLines(reqPath);
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                Matcher matcher = PYTHON_DEPENDENCY.matcher(line);
                if (matcher.find()) {
                    String packageName = matcher.group(1).toLowerCase();
                    String framework = findFrameworkByArtifact(packageName);
                    if (framework != null) {
                        frameworks.add(framework);
                    }
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read requirements.txt", e);
        }
        
        return frameworks;
    }
    

    private String findFrameworkByArtifact(String artifact) {
        String lowerArtifact = artifact.toLowerCase();
        
        if (FRAMEWORK_MAPPING.containsKey(lowerArtifact)) {
            return FRAMEWORK_MAPPING.get(lowerArtifact);
        }
        
        for (Map.Entry<String, String> entry : FRAMEWORK_MAPPING.entrySet()) {
            if (lowerArtifact.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
}
