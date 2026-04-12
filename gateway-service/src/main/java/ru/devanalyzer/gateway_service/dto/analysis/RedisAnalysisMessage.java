package ru.devanalyzer.gateway_service.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record RedisAnalysisMessage(
    @JsonProperty("userId") String userId,
    @JsonProperty("requestId") String requestId,
    @JsonProperty("status") String status,
    @JsonProperty("timestamp") Instant timestamp
) {
}
