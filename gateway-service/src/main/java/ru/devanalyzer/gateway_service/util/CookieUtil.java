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
        Cookie cookie = createCookie("accessToken", token, maxAge);
        response.addCookie(cookie);
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token, int maxAge) {
        Cookie cookie = createCookie("refreshToken", token, maxAge);
        response.addCookie(cookie);
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie("accessToken", "", 0);
        response.addCookie(cookie);
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie("refreshToken", "", 0);
        response.addCookie(cookie);
    }

    public Optional<String> getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "accessToken");
    }

    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "refreshToken");
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        if (!"localhost".equals(domain)) {
            cookie.setDomain(domain);
        }
        return cookie;
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
