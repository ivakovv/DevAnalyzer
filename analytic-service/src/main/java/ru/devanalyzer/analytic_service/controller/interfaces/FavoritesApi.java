package ru.devanalyzer.analytic_service.controller.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import ru.devanalyzer.analytic_service.dto.AnalysisPreviewDto;

import java.util.List;

@Tag(name = "Favorites", description = "API для управления избранными кандидатами")
public interface FavoritesApi {

    @Operation(summary = "Добавить отчет в избранное")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Добавлено в избранное"),
            @ApiResponse(responseCode = "404", description = "Отчет не найден")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<Void> addToFavorites(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "ID запроса анализа") String requestId
    );

    @Operation(summary = "Получить список избранных")
    @ApiResponse(responseCode = "200", description = "Список избранных кандидатов")
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<List<AnalysisPreviewDto>> getFavorites(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "Количество записей") int limit,
            @Parameter(description = "Смещение для пагинации") int offset
    );

    @Operation(summary = "Проверить, находится ли отчет в избранном")
    @ApiResponse(responseCode = "200", description = "true/false")
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<Boolean> isFavorite(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "ID запроса анализа") String requestId
    );

    @Operation(summary = "Удалить из избранного")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Удалено из избранного")
    })
    @SecurityRequirement(name = "Gateway Authentication")
    ResponseEntity<Void> removeFromFavorites(
            @Parameter(hidden = true) Long userId,
            @Parameter(description = "ID запроса анализа") String requestId
    );
}
