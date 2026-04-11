package ru.devanalyzer.gateway_service.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import ru.devanalyzer.gateway_service.security.UserPrincipal;
import ru.devanalyzer.gateway_service.util.CookieUtil;
import ru.devanalyzer.gateway_service.util.JwtUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            var httpRequest = servletRequest.getServletRequest();
            var accessToken = cookieUtil.getAccessTokenFromCookie(httpRequest);

            if (accessToken.isPresent() && jwtUtil.validateToken(accessToken.get())) {
                String token = accessToken.get();
                String email = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                Collection<? extends GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UserPrincipal userPrincipal = new UserPrincipal(userId, email, role, authorities);
                attributes.put("principal", userPrincipal);

                log.info("WebSocket handshake: authenticated user {} (userId: {})", email, userId);
                return true;
            }
        }
        log.warn("WebSocket handshake rejected: no valid JWT token found");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
