package ru.devanalyzer.gateway_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devanalyzer.gateway_service.dto.analysis.AnalysisRequest;
import ru.devanalyzer.gateway_service.dto.analysis.kafka.AnalysisResponseDto;
import ru.devanalyzer.gateway_service.security.UserPrincipal;
import ru.devanalyzer.gateway_service.service.analysis.AnalysisService;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisResponseDto> startAnalysis(@Valid @RequestBody AnalysisRequest request,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.accepted()
                .body(analysisService.startAnalysis(
                        request.githubUsername(), 
                        request.techStack(), 
                        principal.getUserId()
                ));
    }

}
