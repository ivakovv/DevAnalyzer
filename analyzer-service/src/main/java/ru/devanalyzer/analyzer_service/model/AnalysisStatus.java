package ru.devanalyzer.analyzer_service.model;

public enum AnalysisStatus {
    PROCESSING("processing"),
    FILTERING("filtering"),
    ANALYZING("analyzing"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    AnalysisStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
