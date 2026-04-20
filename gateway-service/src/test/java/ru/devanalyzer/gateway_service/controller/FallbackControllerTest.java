package ru.devanalyzer.gateway_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FallbackControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FallbackController())
                .build();
    }

    @Test
    @DisplayName("GET /fallback/user-service - возвращает SERVICE_UNAVAILABLE")
    void userServiceFallback_ReturnsServiceUnavailable() throws Exception {

        mockMvc.perform(get("/fallback/user-service"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("User service is temporarily unavailable"))
                .andExpect(jsonPath("$.message").value("Please try again later"));
    }
}
