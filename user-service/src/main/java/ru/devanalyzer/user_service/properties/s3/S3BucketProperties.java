package ru.devanalyzer.user_service.properties.s3;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "io.reflectoring.aws.s3")
public class S3BucketProperties {

    @NotBlank(message = "S3 bucket name must be configured")
    private String bucketName;

    @Valid
    private PresignedUrl presignedUrl = new PresignedUrl();

    @Getter
    @Setter
    @Validated
    public static class PresignedUrl {

        @NotNull(message = "S3 presigned GET URL validity must be specified")
        @Positive(message = "S3 presigned GET URL validity must be positive")
        private Integer getValidity;

        @NotNull(message = "S3 presigned PUT URL validity must be specified")
        @Positive(message = "S3 presigned PUT URL validity must be positive")
        private Integer putValidity;
    }

    public Duration getPresignedUrlValidity() {
        return Duration.ofSeconds(this.presignedUrl.getValidity);
    }

    public Duration getPresignedPutUrlValidity() {
        return Duration.ofSeconds(this.presignedUrl.putValidity);
    }
}