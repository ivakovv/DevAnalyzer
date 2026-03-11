package ru.devanalyzer.gateway_service.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.devanalyzer.gateway_service.dto.auth.LoginRequestDto;
import ru.devanalyzer.gateway_service.dto.user.UserValidationResponseDto;
import ru.devanalyzer.gateway_service.exception.auth.AuthenticationFailedException;
import ru.devanalyzer.gateway_service.exception.auth.InvalidTokenException;
import ru.devanalyzer.gateway_service.exception.auth.RefreshTokenNotFoundException;
import ru.devanalyzer.gateway_service.service.UserServiceClient;
import ru.devanalyzer.gateway_service.util.CookieUtil;
import ru.devanalyzer.gateway_service.util.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public void login(LoginRequestDto request, HttpServletResponse response) {
        try {
            UserValidationResponseDto user = userServiceClient.validateUser(
                    request.email(),
                    request.password()
            );

            String accessToken = jwtUtil.generateAccessToken(
                    user.email(),
                    user.role(),
                    user.userId()
            );

            String refreshToken = jwtUtil.generateRefreshToken(
                    user.email(),
                    user.role(),
                    user.userId()
            );

            int accessMaxAge = (int) (accessTokenExpiration / 1000);
            int refreshMaxAge = (int) (refreshTokenExpiration / 1000);

            cookieUtil.addAccessTokenCookie(response, accessToken, accessMaxAge);
            cookieUtil.addRefreshTokenCookie(response, refreshToken, refreshMaxAge);

            log.info("User logged in successfully: {}", user.email());
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String email = jwtUtil.extractUsername(refreshToken);
        Long userId = jwtUtil.extractUserId(refreshToken);
        String role = jwtUtil.extractRole(refreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(email, role, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(email, role, userId);

        int accessMaxAge = (int) (accessTokenExpiration / 1000);
        int refreshMaxAge = (int) (refreshTokenExpiration / 1000);

        cookieUtil.addAccessTokenCookie(response, newAccessToken, accessMaxAge);
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken, refreshMaxAge);

        log.info("Tokens refreshed for user: {}", email);
    }

    public void logout(HttpServletResponse response) {
        cookieUtil.deleteAccessTokenCookie(response);
        cookieUtil.deleteRefreshTokenCookie(response);
        log.info("User logged out");
    }
}
