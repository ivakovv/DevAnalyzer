package ru.devanalyzer.analyzer_service.services.analysis.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.analyzer_service.dto.kafka.AnalysisRequestDto;
import ru.devanalyzer.analyzer_service.messaging.AnalysisMessageProducer;
import ru.devanalyzer.analyzer_service.model.AnalysisStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalysisStatusNotifierTest {

    @Mock
    private AnalysisMessageProducer messageProducer;

    @InjectMocks
    private AnalysisStatusNotifier notifier;

    @Captor
    private ArgumentCaptor<String> topicCaptor;

    @Captor
    private ArgumentCaptor<String> requestIdCaptor;

    @Captor
    private ArgumentCaptor<Long> userIdCaptor;

    @Captor
    private ArgumentCaptor<String> statusCaptor;

    private AnalysisRequestDto request;
    private String statusTopic;

    @BeforeEach
    void setUp() {
        statusTopic = "analysis-status-topic";
        ReflectionTestUtils.setField(notifier, "statusTopic", statusTopic);

        request = new AnalysisRequestDto(
                "request-123",
                1L,
                "testuser",
                null,
                null,
                Instant.now()
        );
    }

    @Test
    void notifyStatus_shouldSendMessageWithCorrectTopicAndData() {

        notifier.notifyStatus(request, AnalysisStatus.PROCESSING);

        verify(messageProducer).sendToTopic(
                topicCaptor.capture(),
                requestIdCaptor.capture(),
                userIdCaptor.capture(),
                statusCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo(statusTopic);
        assertThat(requestIdCaptor.getValue()).isEqualTo("request-123");
        assertThat(userIdCaptor.getValue()).isEqualTo(1L);

        assertThat(statusCaptor.getValue()).isEqualTo(AnalysisStatus.PROCESSING.getValue());
    }

    @Test
    void notifyStatus_shouldSendAllStatusTypes() {
        AnalysisStatus[] statuses = {
                AnalysisStatus.PROCESSING,
                AnalysisStatus.FILTERING,
                AnalysisStatus.ANALYZING,
                AnalysisStatus.BUILDING_REPORT,
                AnalysisStatus.COMPLETED
        };

        for (AnalysisStatus status : statuses) {
            notifier.notifyStatus(request, status);

            verify(messageProducer).sendToTopic(
                    statusTopic,
                    "request-123",
                    1L,
                    status.getValue()
            );
        }
    }

    @Test
    void notifyFailed_shouldSendFailedStatus() {
        Exception exception = new RuntimeException("Test error");

        notifier.notifyFailed(request, exception);

        verify(messageProducer).sendToTopic(
                topicCaptor.capture(),
                requestIdCaptor.capture(),
                userIdCaptor.capture(),
                statusCaptor.capture()
        );

        assertThat(topicCaptor.getValue()).isEqualTo(statusTopic);
        assertThat(requestIdCaptor.getValue()).isEqualTo("request-123");
        assertThat(userIdCaptor.getValue()).isEqualTo(1L);
        // Исправлено: ожидаем реальное значение из enum
        assertThat(statusCaptor.getValue()).isEqualTo(AnalysisStatus.FAILED.getValue());
    }

    @Test
    void notifyFailed_shouldHandleNullExceptionMessage() {
        Exception exception = new RuntimeException();

        notifier.notifyFailed(request, exception);
        verify(messageProducer).sendToTopic(
                statusTopic,
                "request-123",
                1L,
                AnalysisStatus.FAILED.getValue()
        );
    }
}
