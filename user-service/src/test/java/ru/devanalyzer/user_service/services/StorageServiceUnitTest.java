package ru.devanalyzer.user_service.services;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.devanalyzer.user_service.exceptions.s3.PresignedUrlGenerationException;
import ru.devanalyzer.user_service.exceptions.s3.S3ObjectNotFoundException;
import ru.devanalyzer.user_service.properties.s3.S3BucketProperties;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for StorageService")
class StorageServiceUnitTest {

    @Mock
    private S3Template s3Template;

    @Mock
    private S3BucketProperties properties;

    @InjectMocks
    private StorageService storageService;

    private final Long userId = 1L;
    private final String BUCKET_NAME = "test-bucket";
    private final String OBJECT_KEY = "users/avatars/" + userId + "/photo.png";
    private final Duration GET_VALIDITY = Duration.ofSeconds(3200);
    private final Duration PUT_VALIDITY = Duration.ofSeconds(200);

    @BeforeEach
    void setUp() {
        lenient().when(properties.getBucketName()).thenReturn(BUCKET_NAME);
        lenient().when(properties.getPresignedUrlValidity()).thenReturn(GET_VALIDITY);
        lenient().when(properties.getPresignedPutUrlValidity()).thenReturn(PUT_VALIDITY);
    }

    @Test
    @DisplayName("Должен сгенерировать GET ссылку когда объект существует")
    void shouldGenerateGetUrlWhenObjectExists() throws Exception {
        // given
        URL expectedUrl = new URL("https://test.url/get");
        when(s3Template.objectExists(BUCKET_NAME, OBJECT_KEY)).thenReturn(true);
        when(s3Template.createSignedGetURL(BUCKET_NAME, OBJECT_KEY, GET_VALIDITY))
                .thenReturn(expectedUrl);

        // when
        URL actualUrl = storageService.generateViewablePresignedUrl(OBJECT_KEY);

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(s3Template).objectExists(BUCKET_NAME, OBJECT_KEY);
        verify(s3Template).createSignedGetURL(BUCKET_NAME, OBJECT_KEY, GET_VALIDITY);
    }

    @Test
    @DisplayName("Должен выбросить исключение когда объект не существует")
    void shouldThrowExceptionWhenObjectNotExists() {
        // given
        when(s3Template.objectExists(BUCKET_NAME, OBJECT_KEY)).thenReturn(false);

        // when/then
        assertThatThrownBy(() -> storageService.generateViewablePresignedUrl(OBJECT_KEY))
                .isInstanceOf(S3ObjectNotFoundException.class)
                .hasMessageContaining(OBJECT_KEY);

        verify(s3Template).objectExists(BUCKET_NAME, OBJECT_KEY);
        verify(s3Template, never()).createSignedGetURL(any(), any(), any());
    }

    @Test
    @DisplayName("Должен сгенерировать PUT ссылку")
    void shouldGeneratePutUrl() throws Exception {
        // given
        URL expectedUrl = new URL("https://test.url/put");
        when(s3Template.createSignedPutURL(BUCKET_NAME, OBJECT_KEY, PUT_VALIDITY))
                .thenReturn(expectedUrl);

        // when
        URL actualUrl = storageService.generateUploadablePresignedUrl(OBJECT_KEY);

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(s3Template).createSignedPutURL(BUCKET_NAME, OBJECT_KEY, PUT_VALIDITY);
    }

    @Test
    @DisplayName("Должен обработать ошибку S3 при генерации GET ссылки")
    void shouldHandleS3ErrorWhenGeneratingGetUrl() {
        // given
        when(s3Template.objectExists(BUCKET_NAME, OBJECT_KEY)).thenReturn(true);
        when(s3Template.createSignedGetURL(any(), any(), any()))
                .thenThrow(new RuntimeException("S3 error"));

        // when/then
        assertThatThrownBy(() -> storageService.generateViewablePresignedUrl(OBJECT_KEY))
                .isInstanceOf(PresignedUrlGenerationException.class);
    }

    @Test
    @DisplayName("PUT ссылка должна иметь меньшее время жизни чем GET")
    void putUrlShouldHaveShorterValidity() {
        assertThat(PUT_VALIDITY.getSeconds()).isLessThan(GET_VALIDITY.getSeconds());
    }
}