package ru.devanalyzer.gateway_service.dto.analysis.websocket;

import java.time.Instant;

public record AnalysisStatusMessage(
        String requestId,
        String status,
        Instant timestamp
) {
}
