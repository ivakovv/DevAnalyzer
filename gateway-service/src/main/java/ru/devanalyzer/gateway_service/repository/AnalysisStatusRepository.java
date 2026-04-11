package ru.devanalyzer.gateway_service.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalysisStatusRepository {

    @Value("${spring.data.redis.ttl.status}")
    private Duration STATUS_TTL;

    @Value("${spring.data.redis.keys.status}")
    private String STATUS_KEY_PREFIX;

    @Value("${spring.data.redis.keys.user-index}")
    private String USER_INDEX_KEY_PREFIX;

    private final RedisTemplate<String, String> redisTemplate;

    public void saveStatus(String requestId, Long userId, AnalysisStatus status) {
        String key = STATUS_KEY_PREFIX + requestId;
        
        redisTemplate.opsForHash().put(key, "userId", userId.toString());
        redisTemplate.opsForHash().put(key, "status", status.getValue());
        redisTemplate.expire(key, STATUS_TTL);
        
        String userIndexKey = USER_INDEX_KEY_PREFIX + userId;
        redisTemplate.opsForSet().add(userIndexKey, requestId);
        redisTemplate.expire(userIndexKey, STATUS_TTL);
        
        log.debug("Saved status '{}' for requestId: {}, userId: {}", status, requestId, userId);
    }
}
