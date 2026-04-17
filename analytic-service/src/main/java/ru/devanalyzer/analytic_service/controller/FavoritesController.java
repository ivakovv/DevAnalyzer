package ru.devanalyzer.analytic_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.devanalyzer.analytic_service.controller.interfaces.FavoritesApi;
import ru.devanalyzer.analytic_service.dto.AnalysisPreviewDto;
import ru.devanalyzer.analytic_service.service.AnalyticService;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoritesController implements FavoritesApi {

    private final AnalyticService analyticService;

    @PostMapping("/{requestId}")
    public ResponseEntity<Void> addToFavorites(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String requestId
    ) {
        analyticService.addToFavorites(userId, requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AnalysisPreviewDto>> getFavorites(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(analyticService.getFavorites(userId, limit, offset));
    }

    @GetMapping("/check/{requestId}")
    public ResponseEntity<Boolean> isFavorite(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String requestId
    ) {
        return ResponseEntity.ok(analyticService.isFavorite(userId, requestId));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> removeFromFavorites(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String requestId
    ) {
        analyticService.removeFromFavorites(userId, requestId);
        return ResponseEntity.ok().build();
    }
}
