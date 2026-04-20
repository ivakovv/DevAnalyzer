package ru.devanalyzer.gateway_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;
import ru.devanalyzer.gateway_service.dto.auth.ForgotPasswordRequest;
import ru.devanalyzer.gateway_service.dto.auth.LoginRequestDto;
import ru.devanalyzer.gateway_service.dto.auth.ResetPasswordRequest;
import ru.devanalyzer.gateway_service.exception.GlobalExceptionHandler;
import ru.devanalyzer.gateway_service.service.auth.AuthenticationService;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter("UTF-8", true);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(filter)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /auth/login - успешный вход")
    void login_Success() throws Exception {

        LoginRequestDto request = new LoginRequestDto("test@example.com", "password123");

        doNothing().when(authenticationService).login(any(LoginRequestDto.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authenticationService).login(eq(request), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("POST /auth/refresh - успешное обновление токенов")
    void refresh_Success() throws Exception {

        doNothing().when(authenticationService).refresh(any(HttpServletRequest.class), any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isOk());

        verify(authenticationService).refresh(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("POST /auth/logout - успешный выход")
    void logout_Success() throws Exception {

        doNothing().when(authenticationService).logout(any(HttpServletResponse.class));

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk());

        verify(authenticationService).logout(any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("POST /auth/forgot-password - успешный запрос сброса пароля")
    void forgotPassword_Success() throws Exception {

        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        doNothing().when(authenticationService).requestPasswordReset("test@example.com");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.string").exists());

        verify(authenticationService).requestPasswordReset("test@example.com");
    }

    @Test
    @DisplayName("POST /auth/forgot-password - валидация: неверный email")
    void forgotPassword_InvalidEmail_BadRequest() throws Exception {

        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid-email");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/reset-password - успешный сброс пароля")
    void resetPassword_Success() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "newPassword123");

        doNothing().when(authenticationService).resetPassword("valid-token", "newPassword123");

        String response = mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);  // Явно указываем UTF-8 при получении строки

        org.assertj.core.api.Assertions.assertThat(response).isEqualTo("?????? ??????? ???????");

        verify(authenticationService).resetPassword("valid-token", "newPassword123");
    }

    @Test
    @DisplayName("POST /auth/reset-password - валидация: короткий пароль")
    void resetPassword_ShortPassword_BadRequest() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "12345");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/reset-password - валидация: пустой токен")
    void resetPassword_EmptyToken_BadRequest() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest("", "newPassword123");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/reset-password - валидация: пустой пароль")
    void resetPassword_EmptyPassword_BadRequest() throws Exception {

        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
