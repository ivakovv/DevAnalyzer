package ru.devanalyzer.gateway_service.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestIdGenerator {

    @Value("${app.request-id.prefix}")
    private String prefix;

    public String generate() {
        return prefix + "_" + UUID.randomUUID();
    }

    public String generateWithType(String type) {
        return prefix + "_" + type + "_" + UUID.randomUUID();
    }
}
