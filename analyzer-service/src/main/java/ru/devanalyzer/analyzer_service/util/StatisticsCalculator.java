package ru.devanalyzer.analyzer_service.util;

import java.util.List;
import java.util.function.Function;

public class StatisticsCalculator {

    public static <T> int sum(List<T> items, Function<T, Integer> mapper) {
        return items.stream()
                .mapToInt(item -> {
                    Integer value = mapper.apply(item);
                    return value != null ? value : 0;
                })
                .sum();
    }

    public static <T> double average(List<T> items, Function<T, Double> mapper) {
        return items.stream()
                .map(mapper)
                .filter(value -> value != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    public static double median(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        List<Integer> sorted = values.stream().sorted().toList();
        int size = sorted.size();

        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    public static double medianDouble(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }

        List<Double> sorted = values.stream().sorted().toList();
        int size = sorted.size();

        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    public static double round(double value, int decimals) {
        double multiplier = Math.pow(10, decimals);
        return Math.round(value * multiplier) / multiplier;
    }
}
