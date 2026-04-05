package ru.devanalyzer.gateway_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalysisResultRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.ttl.result}")
    private Duration RESULT_TTL;

    @Value("${spring.data.redis.keys.result}")
    private String RESULT_KEY_PREFIX;

    public String generateCacheKey(String githubUsername, List<String> techStack) {
        int techStackHash = techStack.stream()
                .sorted()
                .reduce("", (a, b) -> a + b)
                .hashCode();
        return RESULT_KEY_PREFIX + githubUsername + ":" + techStackHash;
    }

    public void saveResult(String githubUsername, List<String> techStack, Object result) {
        String key = generateCacheKey(githubUsername, techStack);
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(key, json, RESULT_TTL);
            log.info("Cached analysis result for github: {}, TTL: {} days", githubUsername, RESULT_TTL.toDays());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize analysis result: {}", e.getMessage());
        }
    }

    public <T> Optional<T> getResult(String githubUsername, List<String> techStack, Class<T> resultClass) {
        String key = generateCacheKey(githubUsername, techStack);
        String json = redisTemplate.opsForValue().get(key);
        
        if (json == null) {
            log.debug("Cache miss for github: {}", githubUsername);
            return Optional.empty();
        }
        
        try {
            T result = objectMapper.readValue(json, resultClass);
            log.info("Cache hit for github: {}", githubUsername);
            return Optional.of(result);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cached result: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
