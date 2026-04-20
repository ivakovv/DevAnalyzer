package ru.devanalyzer.gateway_service.config.websocket;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import ru.devanalyzer.gateway_service.util.CookieUtil;
import ru.devanalyzer.gateway_service.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthenticationInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private ServerHttpRequest serverHttpRequest;

    @Mock
    private ServerHttpResponse serverHttpResponse;

    @Mock
    private WebSocketHandler webSocketHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private WebSocketAuthenticationInterceptor authenticationInterceptor;

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
    }

    @Test
    void beforeHandshake_ValidToken_ReturnsTrue() {

        ServletServerHttpRequest servletRequest = new ServletServerHttpRequest(httpServletRequest);
        String accessToken = "valid-access-token";
        String email = "test@example.com";
        Long userId = 1L;
        String role = "HR";

        when(cookieUtil.getAccessTokenFromCookie(httpServletRequest)).thenReturn(Optional.of(accessToken));
        when(jwtUtil.validateToken(accessToken)).thenReturn(true);
        when(jwtUtil.extractUsername(accessToken)).thenReturn(email);
        when(jwtUtil.extractUserId(accessToken)).thenReturn(userId);
        when(jwtUtil.extractRole(accessToken)).thenReturn(role);

        boolean result = authenticationInterceptor.beforeHandshake(
                servletRequest, serverHttpResponse, webSocketHandler, attributes);

        assertThat(result).isTrue();
        assertThat(attributes).containsKey("principal");
    }

    @Test
    void beforeHandshake_NoToken_ReturnsFalse() {

        ServletServerHttpRequest servletRequest = new ServletServerHttpRequest(httpServletRequest);
        when(cookieUtil.getAccessTokenFromCookie(httpServletRequest)).thenReturn(Optional.empty());

        boolean result = authenticationInterceptor.beforeHandshake(
                servletRequest, serverHttpResponse, webSocketHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).doesNotContainKey("principal");
    }

    @Test
    void beforeHandshake_InvalidToken_ReturnsFalse() {

        ServletServerHttpRequest servletRequest = new ServletServerHttpRequest(httpServletRequest);
        String accessToken = "invalid-token";

        when(cookieUtil.getAccessTokenFromCookie(httpServletRequest)).thenReturn(Optional.of(accessToken));
        when(jwtUtil.validateToken(accessToken)).thenReturn(false);

        boolean result = authenticationInterceptor.beforeHandshake(
                servletRequest, serverHttpResponse, webSocketHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).doesNotContainKey("principal");
    }

    @Test
    void beforeHandshake_NotServletRequest_ReturnsFalse() {

        boolean result = authenticationInterceptor.beforeHandshake(
                serverHttpRequest, serverHttpResponse, webSocketHandler, attributes);

        assertThat(result).isFalse();
        assertThat(attributes).doesNotContainKey("principal");

        // проверка, что cookieUtil не вызывался
        verifyNoInteractions(cookieUtil);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void afterHandshake_Success() {
        // не должно выбрасывать исключений
        authenticationInterceptor.afterHandshake(
                serverHttpRequest, serverHttpResponse, webSocketHandler, null);
        authenticationInterceptor.afterHandshake(
                serverHttpRequest, serverHttpResponse, webSocketHandler, new Exception("test"));
    }
}
