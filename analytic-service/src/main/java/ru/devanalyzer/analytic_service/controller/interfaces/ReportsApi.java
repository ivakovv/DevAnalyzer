package ru.devanalyzer.analytic_service.controller.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import ru.devanalyzer.analytic_service.dto.AnalysisPreviewDto;
import ru.devanalyzer.analytic_service.dto.AnalysisReportResponse;

import java.util.List;

@Tag(name = "Reports", description = "API для работы с отчетами анализа")
public interface ReportsApi {

    @Operation(summary = "Получить полный отчет по requestId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Отчет найден"),
            @ApiResponse(responseCode = "403", description = "Нет доступа к чужому отчету"),
            @ApiResponse(responseCode = "404", description = "Отчет не найден")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<AnalysisReportResponse> getReport(
            @Parameter(description = "ID запроса анализа") String requestId,
            @Parameter(hidden = true) Long userId
    );

    @Operation(summary = "Получить историю анализов текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список анализов")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<List<AnalysisPreviewDto>> getHistory(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "Количество записей (по умолчанию 20)") int limit,
            @Parameter(description = "Смещение для пагинации") int offset
    );
}
