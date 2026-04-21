package ru.devanalyzer.gateway_service.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.gateway_service.dto.auth.LoginRequestDto;
import ru.devanalyzer.gateway_service.dto.user.UserValidationResponseDto;
import ru.devanalyzer.gateway_service.exception.auth.AuthenticationFailedException;
import ru.devanalyzer.gateway_service.exception.auth.InvalidTokenException;
import ru.devanalyzer.gateway_service.exception.auth.RefreshTokenNotFoundException;
import ru.devanalyzer.gateway_service.service.UserServiceClient;
import ru.devanalyzer.gateway_service.service.email.EmailService;
import ru.devanalyzer.gateway_service.util.CookieUtil;
import ru.devanalyzer.gateway_service.util.JwtUtil;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(authenticationService, "refreshTokenExpiration", 604800000L);
        ReflectionTestUtils.setField(authenticationService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void login_Success() {

        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password123");
        UserValidationResponseDto user = new UserValidationResponseDto(1L, "test@example.com", "HR");

        when(userServiceClient.validateUser("test@example.com", "password123")).thenReturn(user);
        when(jwtUtil.generateAccessToken("test@example.com", "HR", 1L)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken("test@example.com", "HR", 1L)).thenReturn("refresh-token");


        authenticationService.login(loginRequest, response);


        verify(cookieUtil).addAccessTokenCookie(eq(response), eq("access-token"), eq(900));
        verify(cookieUtil).addRefreshTokenCookie(eq(response), eq("refresh-token"), eq(604800));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {

        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "wrongpassword");

        when(userServiceClient.validateUser("test@example.com", "wrongpassword"))
                .thenThrow(new RuntimeException("Invalid credentials"));


        assertThatThrownBy(() -> authenticationService.login(loginRequest, response))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");

        verify(cookieUtil, never()).addAccessTokenCookie(any(), anyString(), anyInt());
    }

    @Test
    void refresh_Success() {

        String refreshToken = "valid-refresh-token";
        String email = "test@example.com";
        Long userId = 1L;
        String role = "HR";

        when(cookieUtil.getRefreshTokenFromCookie(request)).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn(email);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId);
        when(jwtUtil.extractRole(refreshToken)).thenReturn(role);
        when(jwtUtil.generateAccessToken(email, role, userId)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(email, role, userId)).thenReturn("new-refresh-token");


        authenticationService.refresh(request, response);


        verify(cookieUtil).addAccessTokenCookie(eq(response), eq("new-access-token"), eq(900));
        verify(cookieUtil).addRefreshTokenCookie(eq(response), eq("new-refresh-token"), eq(604800));
    }

    @Test
    void refresh_TokenNotFound_ThrowsException() {

        when(cookieUtil.getRefreshTokenFromCookie(request)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> authenticationService.refresh(request, response))
                .isInstanceOf(RefreshTokenNotFoundException.class)
                .hasMessage("Refresh token not found");
    }

    @Test
    void refresh_InvalidToken_ThrowsException() {

        String refreshToken = "invalid-token";
        when(cookieUtil.getRefreshTokenFromCookie(request)).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);


        assertThatThrownBy(() -> authenticationService.refresh(request, response))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void logout_Success() {

        authenticationService.logout(response);


        verify(cookieUtil).deleteAccessTokenCookie(response);
        verify(cookieUtil).deleteRefreshTokenCookie(response);
    }

    @Test
    void requestPasswordReset_Success() {

        String email = "test@example.com";
        UserValidationResponseDto user = new UserValidationResponseDto(1L, email, "HR");
        String resetToken = "reset-token";
        String resetLink = "http://localhost:3000/reset-password?token=reset-token";

        when(userServiceClient.findByEmail(email)).thenReturn(user);
        when(jwtUtil.generatePasswordResetToken(1L, email)).thenReturn(resetToken);


        authenticationService.requestPasswordReset(email);


        verify(emailService).sendPasswordResetEmail(email, resetLink);
    }

    @Test
    void resetPassword_Success() {

        String token = "valid-reset-token";
        String newPassword = "newPassword123";
        Long userId = 1L;

        when(jwtUtil.getUserIdFromPasswordResetToken(token)).thenReturn(userId);


        authenticationService.resetPassword(token, newPassword);


        verify(userServiceClient).resetPassword(userId, newPassword);
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {

        String token = "invalid-token";
        String newPassword = "newPassword123";

        when(jwtUtil.getUserIdFromPasswordResetToken(token))
                .thenThrow(new IllegalArgumentException("Invalid token"));


        assertThatThrownBy(() -> authenticationService.resetPassword(token, newPassword))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid token");
    }
}
