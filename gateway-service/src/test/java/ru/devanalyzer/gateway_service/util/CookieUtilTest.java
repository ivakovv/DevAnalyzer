package ru.devanalyzer.gateway_service.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CookieUtil cookieUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cookieUtil, "secure", true);
        ReflectionTestUtils.setField(cookieUtil, "domain", "example.com");
    }

    @Test
    void addAccessTokenCookie_Success() {

        String token = "test-access-token";
        int maxAge = 900;


        cookieUtil.addAccessTokenCookie(response, token, maxAge);


        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader).contains("accessToken=test-access-token");
        assertThat(cookieHeader).contains("Max-Age=900");
        assertThat(cookieHeader).contains("Path=/");
        assertThat(cookieHeader).contains("HttpOnly");
        assertThat(cookieHeader).contains("Secure");
        assertThat(cookieHeader).contains("Domain=example.com");
        assertThat(cookieHeader).contains("SameSite=None");
    }

    @Test
    void addRefreshTokenCookie_Success() {

        String token = "test-refresh-token";
        int maxAge = 604800;


        cookieUtil.addRefreshTokenCookie(response, token, maxAge);


        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader).contains("refreshToken=test-refresh-token");
        assertThat(cookieHeader).contains("Max-Age=604800");
    }

    @Test
    void deleteAccessTokenCookie_Success() {

        cookieUtil.deleteAccessTokenCookie(response);


        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader).contains("accessToken=");
        assertThat(cookieHeader).contains("Max-Age=0");
    }

    @Test
    void deleteRefreshTokenCookie_Success() {

        cookieUtil.deleteRefreshTokenCookie(response);


        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader).contains("refreshToken=");
        assertThat(cookieHeader).contains("Max-Age=0");
    }

    @Test
    void getAccessTokenFromCookie_Success() {

        Cookie[] cookies = {
                new Cookie("accessToken", "test-access-token"),
                new Cookie("otherCookie", "other-value")
        };
        when(request.getCookies()).thenReturn(cookies);


        Optional<String> token = cookieUtil.getAccessTokenFromCookie(request);


        assertThat(token).isPresent().contains("test-access-token");
    }

    @Test
    void getAccessTokenFromCookie_NotFound_ReturnsEmpty() {

        Cookie[] cookies = {
                new Cookie("otherCookie", "other-value")
        };
        when(request.getCookies()).thenReturn(cookies);


        Optional<String> token = cookieUtil.getAccessTokenFromCookie(request);


        assertThat(token).isEmpty();    }

    @Test
    void getAccessTokenFromCookie_NullCookies_ReturnsEmpty() {

        when(request.getCookies()).thenReturn(null);


        Optional<String> token = cookieUtil.getAccessTokenFromCookie(request);


        assertThat(token).isEmpty();
    }

    @Test
    void getRefreshTokenFromCookie_Success() {

        Cookie[] cookies = {
                new Cookie("refreshToken", "test-refresh-token"),
                new Cookie("otherCookie", "other-value")
        };
        when(request.getCookies()).thenReturn(cookies);


        Optional<String> token = cookieUtil.getRefreshTokenFromCookie(request);


        assertThat(token).isPresent().contains("test-refresh-token");
    }

    @Test
    void addCookieHeader_Localhost_NoSecureFlag() {

        ReflectionTestUtils.setField(cookieUtil, "secure", true);
        ReflectionTestUtils.setField(cookieUtil, "domain", "localhost");


        cookieUtil.addAccessTokenCookie(response, "test-token", 900);


        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq("Set-Cookie"), headerCaptor.capture());

        String cookieHeader = headerCaptor.getValue();
        assertThat(cookieHeader).contains("Secure");
        assertThat(cookieHeader).doesNotContain("Domain=localhost");
    }
}
