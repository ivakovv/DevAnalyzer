package ru.devanalyzer.gateway_service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalysisStatusRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofDays(30);
    private static final String STATUS_KEY_PREFIX = "analysis:status:";

    public void saveStatus(String requestId, AnalysisStatus status) {
        String key = STATUS_KEY_PREFIX + requestId;
        redisTemplate.opsForValue().set(key, status.getValue(), TTL);
        log.debug("Saved status '{}' for requestId: {}", status, requestId);
    }

    public String getStatus(String requestId) {
        String key = STATUS_KEY_PREFIX + requestId;
        return redisTemplate.opsForValue().get(key);
    }

    public void saveResult(String githubUsername, String techStackHash, String requestId) {
        String key = "analysis:result:" + githubUsername + ":" + techStackHash;
        redisTemplate.opsForValue().set(key, requestId, TTL);
        log.debug("Saved result mapping for github: {}, techStack hash: {} -> requestId: {}", 
                githubUsername, techStackHash, requestId);
    }

    public String findExistingResult(String githubUsername, String techStackHash) {
        String key = "analysis:result:" + githubUsername + ":" + techStackHash;
        return redisTemplate.opsForValue().get(key);
    }
}
