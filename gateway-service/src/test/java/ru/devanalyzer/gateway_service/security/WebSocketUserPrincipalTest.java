package ru.devanalyzer.gateway_service.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketUserPrincipalTest {

    @Test
    void webSocketUserPrincipal_Getters_ReturnCorrectValues() {

        Long userId = 1L;
        String email = "test@example.com";

        WebSocketUserPrincipal principal = new WebSocketUserPrincipal(userId, email);

        assertThat(principal.getUserId()).isEqualTo(userId);
        assertThat(principal.getEmail()).isEqualTo(email);
        assertThat(principal.getName()).isEqualTo("1");
    }

    @Test
    void getName_ReturnsUserIdAsString() {

        Long userId = 12345L;
        WebSocketUserPrincipal principal = new WebSocketUserPrincipal(userId, "test@example.com");

        String name = principal.getName();

        assertThat(name).isEqualTo("12345");
    }
}
