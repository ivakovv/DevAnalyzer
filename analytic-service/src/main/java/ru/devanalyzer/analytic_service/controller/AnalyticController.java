package ru.devanalyzer.analytic_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.devanalyzer.analytic_service.dto.AnalysisReportResponse;
import ru.devanalyzer.analytic_service.exception.AccessDeniedException;
import ru.devanalyzer.analytic_service.service.AnalyticService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class AnalyticController {

    private final AnalyticService analyticService;

    @GetMapping("/{requestId}")
    public ResponseEntity<AnalysisReportResponse> getReport(
            @PathVariable String requestId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        try {
            return analyticService.getReport(requestId, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
