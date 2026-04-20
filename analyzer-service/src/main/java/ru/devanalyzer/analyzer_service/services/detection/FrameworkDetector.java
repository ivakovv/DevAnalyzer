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
            // Frontend Frameworks
            Map.entry("react", "React"),
            Map.entry("react-dom", "React"),
            Map.entry("vue", "Vue.js"),
            Map.entry("@vue/core", "Vue.js"),
            Map.entry("vuejs", "Vue.js"),
            Map.entry("@angular/core", "Angular"),
            Map.entry("angular", "Angular"),
            Map.entry("next", "Next.js"),
            Map.entry("nextjs", "Next.js"),
            
            // Backend Frameworks - Java
            Map.entry("spring-boot", "Spring Boot"),
            Map.entry("spring-boot-starter", "Spring Boot"),
            Map.entry("spring-cloud", "Spring Cloud"),
            Map.entry("spring-security", "Spring Security"),
            Map.entry("spring-data", "Spring Data"),
            Map.entry("spring-web", "Spring Web"),
            Map.entry("spring-webmvc", "Spring MVC"),
            Map.entry("hibernate", "Hibernate"),
            Map.entry("hibernate-core", "Hibernate"),
            
            // Backend Frameworks - Python
            Map.entry("django", "Django"),
            Map.entry("flask", "Flask"),
            Map.entry("fastapi", "FastAPI"),
            
            // Backend Frameworks - Node.js
            Map.entry("express", "Express.js"),
            Map.entry("nestjs", "NestJS"),
            Map.entry("@nestjs/core", "NestJS"),
            Map.entry("@nestjs/common", "NestJS"),
            Map.entry("nest", "NestJS"),
            Map.entry("nodejs", "Node.js"),
            Map.entry("node", "Node.js"),
            
            // Backend Frameworks - .NET
            Map.entry("aspnetcore", "ASP.NET Core"),
            Map.entry("asp.net", "ASP.NET Core"),
            Map.entry("microsoft.aspnetcore", "ASP.NET Core"),
            Map.entry("dotnet", "ASP.NET Core"),
            
            // Backend Frameworks - PHP/Ruby
            Map.entry("laravel", "Laravel"),
            Map.entry("laravel/framework", "Laravel"),
            Map.entry("rails", "Ruby on Rails"),
            Map.entry("ruby-on-rails", "Ruby on Rails"),
            Map.entry("railties", "Ruby on Rails"),
            
            // Databases - SQL
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("postgres", "PostgreSQL"),
            Map.entry("pg", "PostgreSQL"),
            Map.entry("psycopg2", "PostgreSQL"),
            Map.entry("mysql", "MySQL"),
            Map.entry("mysql-connector", "MySQL"),
            Map.entry("mysql2", "MySQL"),
            Map.entry("pymysql", "MySQL"),
            
            // Databases - NoSQL
            Map.entry("mongodb", "MongoDB"),
            Map.entry("mongo", "MongoDB"),
            Map.entry("mongoose", "MongoDB"),
            Map.entry("pymongo", "MongoDB"),
            Map.entry("redis", "Redis"),
            Map.entry("redis-py", "Redis"),
            Map.entry("ioredis", "Redis"),
            Map.entry("cassandra", "Cassandra"),
            Map.entry("cassandra-driver", "Cassandra"),
            Map.entry("elasticsearch", "Elasticsearch"),
            Map.entry("elastic", "Elasticsearch"),
            Map.entry("@elastic/elasticsearch", "Elasticsearch"),
            
            // Message Brokers
            Map.entry("kafka", "Kafka"),
            Map.entry("apache-kafka", "Kafka"),
            Map.entry("spring-kafka", "Kafka"),
            Map.entry("kafkajs", "Kafka"),
            Map.entry("kafka-python", "Kafka"),
            Map.entry("rabbitmq", "RabbitMQ"),
            Map.entry("amqp", "RabbitMQ"),
            Map.entry("amqplib", "RabbitMQ"),
            Map.entry("pika", "RabbitMQ"),
            
            // DevOps & Infrastructure
            Map.entry("docker", "Docker"),
            Map.entry("kubernetes", "Kubernetes"),
            Map.entry("k8s", "Kubernetes"),
            Map.entry("terraform", "Terraform"),
            Map.entry("jenkins", "Jenkins"),
            Map.entry("github-actions", "GitHub Actions"),
            
            // Cloud Platforms
            Map.entry("aws", "AWS"),
            Map.entry("aws-sdk", "AWS"),
            Map.entry("@aws-sdk", "AWS"),
            Map.entry("boto3", "AWS"),
            Map.entry("ec2", "AWS (EC2)"),
            Map.entry("s3", "AWS (S3)"),
            Map.entry("lambda", "AWS (Lambda)"),
            Map.entry("aws-lambda", "AWS (Lambda)"),
            Map.entry("gcp", "Google Cloud Platform"),
            Map.entry("google-cloud", "Google Cloud Platform"),
            Map.entry("@google-cloud", "Google Cloud Platform"),
            Map.entry("azure", "Azure"),
            Map.entry("microsoft-azure", "Azure"),
            Map.entry("@azure", "Azure"),
            
            // API & GraphQL
            Map.entry("graphql", "GraphQL"),
            Map.entry("apollo", "GraphQL"),
            Map.entry("@apollo/server", "GraphQL"),
            Map.entry("apollo-server", "GraphQL"),
            Map.entry("graphene", "GraphQL"),
            
            // Build Tools & Bundlers
            Map.entry("webpack", "Webpack"),
            Map.entry("vite", "Vite"),
            Map.entry("vitejs", "Vite"),
            Map.entry("@vitejs/plugin", "Vite"),
            
            // CSS Frameworks
            Map.entry("tailwindcss", "Tailwind CSS"),
            Map.entry("tailwind", "Tailwind CSS"),
            
            // Additional Libraries
            Map.entry("typescript", "TypeScript"),
            Map.entry("redux", "Redux"),
            Map.entry("lombok", "Lombok"),
            Map.entry("mapstruct", "MapStruct"),
            Map.entry("junit", "JUnit"),
            Map.entry("mockito", "Mockito"),
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
            if (Files.exists(path.resolve("build.gradle")) || Files.exists(path.resolve("build.gradle.kts"))) {
                frameworks.addAll(detectFromGradle(path));
            }
            
            // JavaScript/TypeScript
            if (Files.exists(path.resolve("package.json"))) {
                frameworks.addAll(detectFromNpm(path));
            }
            
            // Python
            if (Files.exists(path.resolve("requirements.txt"))) {
                frameworks.addAll(detectFromPython(path));
            }
            if (Files.exists(path.resolve("Pipfile")) || Files.exists(path.resolve("pyproject.toml"))) {
                frameworks.addAll(detectFromPythonAlt(path));
            }
            
            // .NET
            if (Files.exists(path.resolve("*.csproj")) || containsFile(path, ".csproj")) {
                frameworks.addAll(detectFromDotNet(path));
            }
            
            // PHP
            if (Files.exists(path.resolve("composer.json"))) {
                frameworks.addAll(detectFromComposer(path));
            }
            
            // Ruby
            if (Files.exists(path.resolve("Gemfile"))) {
                frameworks.addAll(detectFromGemfile(path));
            }
            
            // DevOps & Infrastructure
            if (Files.exists(path.resolve("Dockerfile")) || Files.exists(path.resolve("docker-compose.yml")) || Files.exists(path.resolve("docker-compose.yaml"))) {
                frameworks.add("Docker");
            }
            if (containsDirectory(path, "k8s") || containsDirectory(path, "kubernetes") || containsFile(path, ".yaml") || containsFile(path, ".yml")) {
                if (checkKubernetesFiles(path)) {
                    frameworks.add("Kubernetes");
                }
            }
            if (containsFile(path, ".tf") || Files.exists(path.resolve("terraform"))) {
                frameworks.add("Terraform");
            }
            if (Files.exists(path.resolve("Jenkinsfile"))) {
                frameworks.add("Jenkins");
            }
            if (Files.exists(path.resolve(".github/workflows")) || containsDirectory(path, ".github/workflows")) {
                frameworks.add("GitHub Actions");
            }
            
        } catch (Exception e) {
            log.warn("Error detecting frameworks from files: {}", projectPath, e);
        }
        
        return frameworks;
    }
    
    private boolean containsFile(Path path, String extension) {
        try {
            return Files.walk(path, 2)
                    .anyMatch(p -> p.toString().endsWith(extension));
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean containsDirectory(Path path, String dirName) {
        try {
            return Files.walk(path, 2)
                    .anyMatch(p -> Files.isDirectory(p) && p.getFileName().toString().equals(dirName));
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean checkKubernetesFiles(Path path) {
        try {
            return Files.walk(path, 3)
                    .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                    .anyMatch(p -> {
                        try {
                            String content = Files.readString(p);
                            return content.contains("apiVersion:") && (content.contains("kind: Deployment") || content.contains("kind: Service"));
                        } catch (IOException e) {
                            return false;
                        }
                    });
        } catch (IOException e) {
            return false;
        }
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
    
    private Set<String> detectFromPythonAlt(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            // Check Pipfile
            Path pipfile = projectPath.resolve("Pipfile");
            if (Files.exists(pipfile)) {
                String content = Files.readString(pipfile);
                for (Map.Entry<String, String> entry : FRAMEWORK_MAPPING.entrySet()) {
                    if (content.toLowerCase().contains(entry.getKey())) {
                        frameworks.add(entry.getValue());
                    }
                }
            }
            
            // Check pyproject.toml
            Path pyproject = projectPath.resolve("pyproject.toml");
            if (Files.exists(pyproject)) {
                String content = Files.readString(pyproject);
                for (Map.Entry<String, String> entry : FRAMEWORK_MAPPING.entrySet()) {
                    if (content.toLowerCase().contains(entry.getKey())) {
                        frameworks.add(entry.getValue());
                    }
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read Python alternative files", e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromGradle(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Path gradlePath = Files.exists(projectPath.resolve("build.gradle")) 
                ? projectPath.resolve("build.gradle") 
                : projectPath.resolve("build.gradle.kts");
                
            String content = Files.readString(gradlePath);
            
            for (Map.Entry<String, String> entry : FRAMEWORK_MAPPING.entrySet()) {
                if (content.toLowerCase().contains(entry.getKey())) {
                    frameworks.add(entry.getValue());
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read build.gradle", e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromDotNet(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Files.walk(projectPath, 2)
                .filter(p -> p.toString().endsWith(".csproj"))
                .forEach(csproj -> {
                    try {
                        String content = Files.readString(csproj);
                        if (content.contains("Microsoft.AspNetCore") || content.contains("AspNetCore")) {
                            frameworks.add("ASP.NET Core");
                        }
                    } catch (IOException e) {
                        log.debug("Could not read .csproj file", e);
                    }
                });
        } catch (IOException e) {
            log.debug("Could not scan for .csproj files", e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromComposer(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Path composerJson = projectPath.resolve("composer.json");
            String content = Files.readString(composerJson);
            
            if (content.contains("laravel/framework")) {
                frameworks.add("Laravel");
            }
            
            for (Map.Entry<String, String> entry : FRAMEWORK_MAPPING.entrySet()) {
                if (content.toLowerCase().contains(entry.getKey())) {
                    frameworks.add(entry.getValue());
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read composer.json", e);
        }
        
        return frameworks;
    }
    
    private Set<String> detectFromGemfile(Path projectPath) {
        Set<String> frameworks = new HashSet<>();
        
        try {
            Path gemfile = projectPath.resolve("Gemfile");
            String content = Files.readString(gemfile);
            
            if (content.contains("gem 'rails'") || content.contains("gem \"rails\"")) {
                frameworks.add("Ruby on Rails");
            }
            
            for (Map.Entry<String, String> entry : FRAMEWORK_MAPPING.entrySet()) {
                if (content.toLowerCase().contains(entry.getKey())) {
                    frameworks.add(entry.getValue());
                }
            }
            
        } catch (IOException e) {
            log.debug("Could not read Gemfile", e);
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
