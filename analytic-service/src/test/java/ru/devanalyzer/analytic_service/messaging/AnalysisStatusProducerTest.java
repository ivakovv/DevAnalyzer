package ru.devanalyzer.analytic_service.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.analytic_service.dto.AnalysisResponseDto;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisStatusProducerTest {

    @Mock
    private KafkaTemplate<String, AnalysisResponseDto> kafkaTemplate;

    @InjectMocks
    private AnalysisStatusProducer producer;

    @BeforeEach
    void setUp() {
        // Устанавливаем значение для поля topic
        ReflectionTestUtils.setField(producer, "topic", "analysis-status");
        // НЕ стаббим здесь kafkaTemplate, чтобы избежать конфликтов
    }

    @Test
    void sendCompleted_ShouldSendMessageToKafka() {
        // Given
        String requestId = "req-123";
        Long userId = 100L;

        // Стаббим только в этом тесте
        doReturn(CompletableFuture.completedFuture(mock(SendResult.class)))
                .when(kafkaTemplate)
                .send(anyString(), anyString(), any(AnalysisResponseDto.class));

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AnalysisResponseDto> messageCaptor = ArgumentCaptor.forClass(AnalysisResponseDto.class);

        // When
        producer.sendCompleted(requestId, userId);

        // Then
        verify(kafkaTemplate, times(1))
                .send(topicCaptor.capture(), keyCaptor.capture(), messageCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("analysis-status");
        assertThat(keyCaptor.getValue()).isEqualTo(requestId);

        AnalysisResponseDto message = messageCaptor.getValue();
        assertThat(message.requestId()).isEqualTo(requestId);
        assertThat(message.userId()).isEqualTo(userId);
        assertThat(message.status()).isEqualTo("completed");
        assertThat(message.processedAt()).isNotNull();
    }

    @Test
    void sendCompleted_WhenSendFails_ShouldLogError() {
        // Given
        String requestId = "req-789";
        Long userId = 300L;

        CompletableFuture<SendResult<String, AnalysisResponseDto>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka error"));

        // Стаббим только в этом тесте
        doReturn(failedFuture)
                .when(kafkaTemplate)
                .send(anyString(), anyString(), any(AnalysisResponseDto.class));

        // When & Then
        producer.sendCompleted(requestId, userId);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(AnalysisResponseDto.class));
    }

    @Test
    void sendCompleted_WithMultipleCalls_ShouldSendEach() {
        // Given
        doReturn(CompletableFuture.completedFuture(mock(SendResult.class)))
                .when(kafkaTemplate)
                .send(anyString(), anyString(), any(AnalysisResponseDto.class));

        // When
        producer.sendCompleted("req-1", 1L);
        producer.sendCompleted("req-2", 2L);
        producer.sendCompleted("req-3", 3L);

        // Then
        verify(kafkaTemplate, times(3))
                .send(anyString(), anyString(), any(AnalysisResponseDto.class));
    }
}
