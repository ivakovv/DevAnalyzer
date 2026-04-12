package ru.devanalyzer.analyzer_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import ru.devanalyzer.analyzer_service.dto.GitHubRepo;
import ru.devanalyzer.analyzer_service.dto.GitHubStats;

import java.util.List;

@Tag(name = "GitHub", description = "API для получения статистики GitHub пользователей")
public interface GitHubStatsApi {

    @Operation(summary = "Получить статистику пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статистика успешно получена"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден на GitHub"),
            @ApiResponse(responseCode = "500", description = "Ошибка при получении данных с GitHub")
    })
    ResponseEntity<GitHubStats> getStats(@Parameter(description = "Имя пользователя GitHub") String username);

    @Operation(summary = "Получить все репозитории пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Репозитории успешно получены"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден на GitHub"),
            @ApiResponse(responseCode = "500", description = "Ошибка при получении данных с GitHub")
    })
    ResponseEntity<List<GitHubRepo>> getRepositories(@Parameter(description = "Имя пользователя GitHub") String username);
}
