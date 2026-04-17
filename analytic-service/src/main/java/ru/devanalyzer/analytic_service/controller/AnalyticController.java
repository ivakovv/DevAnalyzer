package ru.devanalyzer.analytic_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.analytic_service.controller.interfaces.ReportsApi;
import ru.devanalyzer.analytic_service.dto.AnalysisPreviewDto;
import ru.devanalyzer.analytic_service.dto.AnalysisReportResponse;
import ru.devanalyzer.analytic_service.service.AnalyticService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class AnalyticController implements ReportsApi {

    private final AnalyticService analyticService;

    @GetMapping("/{requestId}")
    public ResponseEntity<AnalysisReportResponse> getReport(
            @PathVariable String requestId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        return analyticService.getReport(requestId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AnalysisPreviewDto>> getHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(analyticService.getHistory(userId, limit, offset));
    }
}
