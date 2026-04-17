package ru.devanalyzer.analytic_service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record WeekActivityDto(
        @JsonProperty("weekStart") LocalDate weekStart,
        @JsonProperty("days") int[] days,
        @JsonProperty("total") int total
) {
    @JsonCreator
    public WeekActivityDto {}
}
