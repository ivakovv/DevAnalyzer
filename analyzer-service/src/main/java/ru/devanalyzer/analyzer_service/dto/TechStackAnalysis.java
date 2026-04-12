package ru.devanalyzer.analyzer_service.dto;

import java.util.List;

public record TechStackAnalysis(
        List<String> requestedFilters,
        List<String> foundTechStack,
        List<String> notFoundTechStack,
        Integer percentageFound
) {
}
