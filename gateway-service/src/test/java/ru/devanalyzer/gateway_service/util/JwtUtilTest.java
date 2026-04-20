package ru.devanalyzer.gateway_service.util;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "thisIsASecretKeyForJwtTokenGenerationAndValidationTestingPurposesOnlyThisIsLongEnoughForHS256";
    private static final Long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 минут
    private static final Long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 дней
    private static final Long PASSWORD_RESET_EXPIRATION = 3600000L; // 1 час

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "passwordResetExpiration", PASSWORD_RESET_EXPIRATION);
    }

    @Test
    void generateAndValidateAccessToken_Success() {

        String username = "test@example.com";
        String role = "HR";
        Long userId = 1L;


        String token = jwtUtil.generateAccessToken(username, role, userId);


        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isTrue();
        assertThat(jwtUtil.isRefreshToken(token)).isFalse();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);
        assertThat(jwtUtil.extractRole(token)).isEqualTo(role);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void generateAndValidateRefreshToken_Success() {

        String username = "test@example.com";
        String role = "HR";
        Long userId = 1L;


        String token = jwtUtil.generateRefreshToken(username, role, userId);


        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.isRefreshToken(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isFalse();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(username);
        assertThat(jwtUtil.extractRole(token)).isEqualTo(role);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void generateAndValidatePasswordResetToken_Success() {

        Long userId = 1L;
        String email = "test@example.com";


        String token = jwtUtil.generatePasswordResetToken(userId, email);


        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.isPasswordResetToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo(email);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {

        String invalidToken = "invalid.token.string";


        boolean isValid = jwtUtil.validateToken(invalidToken);


        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {

        boolean isValid = jwtUtil.validateToken(null);


        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {

        boolean isValid = jwtUtil.validateToken("");


        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {

        String token = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);


        boolean isExpired = jwtUtil.isTokenExpired(token);


        assertThat(isExpired).isFalse();
    }

    @Test
    void getUserIdFromPasswordResetToken_Success() {

        Long userId = 123L;
        String email = "test@example.com";
        String token = jwtUtil.generatePasswordResetToken(userId, email);


        Long extractedUserId = jwtUtil.getUserIdFromPasswordResetToken(token);


        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void getUserIdFromPasswordResetToken_InvalidToken_ThrowsException() {

        String invalidToken = "invalid.token.string";


        assertThatThrownBy(() -> jwtUtil.getUserIdFromPasswordResetToken(invalidToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void getUserIdFromPasswordResetToken_ExpiredToken_ThrowsException() {
        // отрицательное время, чтобы токен создавался уже истекшим
        ReflectionTestUtils.setField(jwtUtil, "passwordResetExpiration", -1000L);
        String token = jwtUtil.generatePasswordResetToken(1L, "test@example.com");

        // вернет false и выбросится "Invalid token"
        assertThatThrownBy(() -> jwtUtil.getUserIdFromPasswordResetToken(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // отрицательное время, чтобы токен создавался уже истекшим
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L);
        String token = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);


        boolean isValid = jwtUtil.validateToken(token);


        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_ExpiredToken_ThrowsExpiredJwtException() {
        // отрицательное время, чтобы токен создавался уже истекшим
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L);
        String token = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);


        assertThatThrownBy(() -> jwtUtil.isTokenExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractAllClaims_ExpiredToken_ThrowsExpiredJwtException() {
        // отрицательное время, чтобы токен создавался уже истекшим
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L);
        String token = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);


        assertThatThrownBy(() -> {
            var method = JwtUtil.class.getDeclaredMethod("extractAllClaims", String.class);
            method.setAccessible(true);
            method.invoke(jwtUtil, token);
        }).hasCauseInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void getUserIdFromPasswordResetToken_WrongTokenType_ThrowsException() {

        String accessToken = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);


        assertThatThrownBy(() -> jwtUtil.getUserIdFromPasswordResetToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token is not a password reset token");
    }

    @Test
    void extractTokenType_Success() {

        String accessToken = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);
        String refreshToken = jwtUtil.generateRefreshToken("test@example.com", "HR", 1L);
        String resetToken = jwtUtil.generatePasswordResetToken(1L, "test@example.com");


        assertThat(jwtUtil.extractTokenType(accessToken)).isEqualTo("ACCESS");
        assertThat(jwtUtil.extractTokenType(refreshToken)).isEqualTo("REFRESH");
        assertThat(jwtUtil.extractTokenType(resetToken)).isEqualTo("PASSWORD_RESET");
    }

    @Test
    void isAccessToken_Success() {

        String accessToken = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);
        String refreshToken = jwtUtil.generateRefreshToken("test@example.com", "HR", 1L);
        String resetToken = jwtUtil.generatePasswordResetToken(1L, "test@example.com");


        assertThat(jwtUtil.isAccessToken(accessToken)).isTrue();
        assertThat(jwtUtil.isAccessToken(refreshToken)).isFalse();
        assertThat(jwtUtil.isAccessToken(resetToken)).isFalse();
    }

    @Test
    void isRefreshToken_Success() {

        String accessToken = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);
        String refreshToken = jwtUtil.generateRefreshToken("test@example.com", "HR", 1L);
        String resetToken = jwtUtil.generatePasswordResetToken(1L, "test@example.com");


        assertThat(jwtUtil.isRefreshToken(accessToken)).isFalse();
        assertThat(jwtUtil.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtUtil.isRefreshToken(resetToken)).isFalse();
    }

    @Test
    void isPasswordResetToken_Success() {

        String accessToken = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);
        String refreshToken = jwtUtil.generateRefreshToken("test@example.com", "HR", 1L);
        String resetToken = jwtUtil.generatePasswordResetToken(1L, "test@example.com");


        assertThat(jwtUtil.isPasswordResetToken(accessToken)).isFalse();
        assertThat(jwtUtil.isPasswordResetToken(refreshToken)).isFalse();
        assertThat(jwtUtil.isPasswordResetToken(resetToken)).isTrue();
    }

    @Test
    void extractExpiration_ValidToken_ReturnsDate() {

        String token = jwtUtil.generateAccessToken("test@example.com", "HR", 1L);


        Date expiration = jwtUtil.extractExpiration(token);


        assertThat(expiration).isNotNull();
        assertThat(expiration.after(new Date())).isTrue();
    }
}
