package ru.devanalyzer.gateway_service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdGeneratorTest {

    private RequestIdGenerator requestIdGenerator;

    @BeforeEach
    void setUp() {
        requestIdGenerator = new RequestIdGenerator();
        ReflectionTestUtils.setField(requestIdGenerator, "prefix", "DEV");
    }

    @Test
    void generate_Success() {

        String requestId = requestIdGenerator.generate();


        assertThat(requestId).startsWith("DEV_");
        assertThat(requestId).matches("DEV_[a-f0-9\\-]{36}");
    }

    @Test
    void generateWithType_Success() {

        String requestId = requestIdGenerator.generateWithType("ANALYSIS");


        assertThat(requestId).startsWith("DEV_ANALYSIS_");
        assertThat(requestId).matches("DEV_ANALYSIS_[a-f0-9\\-]{36}");
    }

    @Test
    void generate_UniqueIds() {

        String id1 = requestIdGenerator.generate();
        String id2 = requestIdGenerator.generate();
        String id3 = requestIdGenerator.generate();


        assertThat(id1).isNotEqualTo(id2).isNotEqualTo(id3);
        assertThat(id2).isNotEqualTo(id3);
    }
}
