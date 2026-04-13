package ru.devanalyzer.gateway_service.model;

public enum AnalysisStatus {

    // Общий статус обработки
    PROCESSING("processing"),

    //Фильтрация репозиториев
    FILTERING("filtering"),
    
    // Полный анализ кода через Sonar
    ANALYZING("analyzing"),

    //Формируем отчет
    BUILDING_REPORT("building_report"),
    
    // Анализ успешно завершен
    COMPLETED("completed"),
    
    // Ошибка при анализе
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
