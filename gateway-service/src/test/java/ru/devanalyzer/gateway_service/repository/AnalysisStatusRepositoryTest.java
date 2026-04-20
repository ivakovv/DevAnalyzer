package ru.devanalyzer.gateway_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.gateway_service.model.AnalysisStatus;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisStatusRepositoryTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private AnalysisStatusRepository analysisStatusRepository;

    @BeforeEach
    void setUp() {
        Duration ttl = Duration.ofHours(24);
        ReflectionTestUtils.setField(analysisStatusRepository, "STATUS_TTL", ttl);
        ReflectionTestUtils.setField(analysisStatusRepository, "STATUS_KEY_PREFIX", "status:");
        ReflectionTestUtils.setField(analysisStatusRepository, "USER_INDEX_KEY_PREFIX", "user:");
    }

    @Test
    void saveStatus_Success() {

        String requestId = "req-123";
        Long userId = 1L;
        AnalysisStatus status = AnalysisStatus.PROCESSING;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        analysisStatusRepository.saveStatus(requestId, userId, status);

        String statusKey = "status:req-123";
        String userIndexKey = "user:1";

        verify(hashOperations).put(statusKey, "userId", "1");
        verify(hashOperations).put(statusKey, "status", "processing");
        verify(redisTemplate).expire(eq(statusKey), any(Duration.class));
        verify(setOperations).add(userIndexKey, requestId);
        verify(redisTemplate).expire(eq(userIndexKey), any(Duration.class));
    }

    @Test
    void saveStatus_Completed_Success() {

        String requestId = "req-456";
        Long userId = 2L;
        AnalysisStatus status = AnalysisStatus.COMPLETED;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        analysisStatusRepository.saveStatus(requestId, userId, status);

        verify(hashOperations).put("status:req-456", "status", "completed");
    }

    @Test
    void saveStatus_Failed_Success() {

        String requestId = "req-789";
        Long userId = 3L;
        AnalysisStatus status = AnalysisStatus.FAILED;

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        analysisStatusRepository.saveStatus(requestId, userId, status);

        verify(hashOperations).put("status:req-789", "status", "failed");
    }
}
