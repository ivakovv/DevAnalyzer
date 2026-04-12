package ru.devanalyzer.gateway_service.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${cookie.domain}")
    private String domain;

    public void addAccessTokenCookie(HttpServletResponse response, String token, int maxAge) {
        addCookieHeader(response, "accessToken", token, maxAge);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token, int maxAge) {
        addCookieHeader(response, "refreshToken", token, maxAge);
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        addCookieHeader(response, "accessToken", "", 0);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        addCookieHeader(response, "refreshToken", "", 0);
    }

    public Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "accessToken");
    }

    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "refreshToken");
    }

    private void addCookieHeader(HttpServletResponse response, String name, String value, int maxAge) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value).append("; ");
        sb.append("Max-Age=").append(maxAge).append("; ");
        sb.append("Path=/; ");
        sb.append("HttpOnly; ");
        if (secure) {
            sb.append("Secure; ");
        }
        if (!"localhost".equals(domain)) {
            sb.append("Domain=").append(domain).append("; ");
        }
        sb.append("SameSite=None");
        response.addHeader("Set-Cookie", sb.toString());
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
