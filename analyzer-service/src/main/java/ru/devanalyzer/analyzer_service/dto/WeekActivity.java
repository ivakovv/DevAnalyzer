package ru.devanalyzer.analyzer_service.dto;


import java.time.LocalDate;

public record WeekActivity(LocalDate weekStart, int[] days, int total) {}