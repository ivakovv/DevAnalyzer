package ru.devanalyzer.analytic_service.dto;

import java.util.List;

public record TechStackAnalysisDto(
        List<String> requestedFilters,
        List<String> foundTechStack,
        List<String> notFoundTechStack,
        Integer percentageFound
) {}
