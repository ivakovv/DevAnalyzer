package ru.devanalyzer.analyzer_service.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class StatisticsCalculatorTest {

    @Test
    void sum_shouldReturnZero_whenListIsEmpty() {
        List<String> items = List.of();

        int result = StatisticsCalculator.sum(items, String::length);

        assertThat(result).isZero();
    }

    @Test
    void sum_shouldCalculateCorrectly() {
        List<String> items = List.of("a", "bb", "ccc");

        int result = StatisticsCalculator.sum(items, String::length);

        assertThat(result).isEqualTo(6);
    }

    @Test
    void sum_shouldHandleNullValues() {
        record Item(Integer value) {}
        List<Item> items = List.of(new Item(5), new Item(null), new Item(10));

        int result = StatisticsCalculator.sum(items, Item::value);

        assertThat(result).isEqualTo(15);
    }

    @Test
    void average_shouldReturnZero_whenListIsEmpty() {
        List<String> items = List.of();

        double result = StatisticsCalculator.average(items, s -> (double) s.length());

        assertThat(result).isZero();
    }

    @Test
    void average_shouldCalculateCorrectly() {
        List<String> items = List.of("a", "bb", "ccc");

        double result = StatisticsCalculator.average(items, s -> (double) s.length());

        assertThat(result).isEqualTo(2.0);
    }

    @Test
    void average_shouldIgnoreNullValues() {
        record Item(Double value) {}
        List<Item> items = List.of(new Item(10.0), new Item(null), new Item(20.0));

        double result = StatisticsCalculator.average(items, Item::value);

        assertThat(result).isEqualTo(15.0);
    }

    @ParameterizedTest
    @MethodSource("medianIntegerProvider")
    void median_shouldCalculateCorrectly(List<Integer> input, double expected) {
        double result = StatisticsCalculator.median(input);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> medianIntegerProvider() {
        return Stream.of(
                Arguments.of(null, 0.0),
                Arguments.of(List.of(), 0.0),
                Arguments.of(List.of(5), 5.0),
                Arguments.of(List.of(1, 2, 3, 4, 5), 3.0),
                Arguments.of(List.of(1, 2, 3, 4), 2.5),
                Arguments.of(List.of(5, 2, 8, 1, 9), 5.0),
                Arguments.of(List.of(10, 20, 30, 40, 50, 60), 35.0)
        );
    }

    @ParameterizedTest
    @MethodSource("medianDoubleProvider")
    void medianDouble_shouldCalculateCorrectly(List<Double> input, double expected) {
        double result = StatisticsCalculator.medianDouble(input);

        assertThat(result).isCloseTo(expected, within(0.001));
    }

    static Stream<Arguments> medianDoubleProvider() {
        return Stream.of(
                Arguments.of(null, 0.0),
                Arguments.of(List.of(), 0.0),
                Arguments.of(List.of(5.5), 5.5),
                Arguments.of(List.of(1.1, 2.2, 3.3, 4.4, 5.5), 3.3),
                Arguments.of(List.of(1.5, 2.5, 3.5, 4.5), 3.0),
                Arguments.of(List.of(10.5, 20.5, 30.5), 20.5)
        );
    }

    @Test
    void round_shouldRoundToSpecifiedDecimals() {
        assertThat(StatisticsCalculator.round(3.14159, 2)).isEqualTo(3.14);
        assertThat(StatisticsCalculator.round(3.14159, 3)).isEqualTo(3.142);
        assertThat(StatisticsCalculator.round(3.14159, 0)).isEqualTo(3.0);
    }

    @ParameterizedTest
    @MethodSource("percentageProvider")
    void calculatePercentage_shouldCalculateCorrectly(int num1, int num2, int expected) {
        int result = StatisticsCalculator.calculatePercentage(num1, num2);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> percentageProvider() {
        return Stream.of(
                Arguments.of(0, 100, 0),
                Arguments.of(50, 100, 50),
                Arguments.of(100, 100, 100),
                Arguments.of(1, 3, 33),
                Arguments.of(2, 3, 67),
                Arguments.of(0, 0, 0)
        );
    }
}
