package ru.devanalyzer.user_service.services;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.devanalyzer.user_service.exceptions.s3.PresignedUrlGenerationException;
import ru.devanalyzer.user_service.properties.s3.S3BucketProperties;

import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(S3BucketProperties.class)
public class StorageService {

    private final S3Template s3Template;
    private final S3BucketProperties s3BucketProperties;

    public URL generateViewablePresignedUrl(String objectKey) {
        try {
            return s3Template.createSignedGetURL(
                    s3BucketProperties.getBucketName(),
                    objectKey,
                    s3BucketProperties.getPresignedUrlValidity()
            );
        } catch (Exception e) {
            log.error("Failed to generate GET presigned URL for: {}", objectKey, e);
            throw new PresignedUrlGenerationException("Failed to generate download URL for objectKey " + objectKey);
        }
    }

    public URL generateUploadablePresignedUrl(String objectKey) {
        try {
            return s3Template.createSignedPutURL(
                    s3BucketProperties.getBucketName(),
                    objectKey,
                    s3BucketProperties.getPresignedPutUrlValidity()
            );
        } catch (Exception e) {
            log.error("Failed to generate PUT presigned URL for: {}", objectKey, e);
            throw new PresignedUrlGenerationException("Failed to generate upload URL for objectKey " + objectKey);
        }
    }
}
