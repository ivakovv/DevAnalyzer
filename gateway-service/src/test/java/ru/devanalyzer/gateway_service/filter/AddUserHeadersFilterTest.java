package ru.devanalyzer.gateway_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.devanalyzer.gateway_service.security.UserPrincipal;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddUserHeadersFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AddUserHeadersFilter addUserHeadersFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_AuthenticatedUser_AddsHeaders() throws ServletException, IOException {

        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "HR",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        addUserHeadersFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), any(HttpServletResponse.class));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getHeader("X-User-Id")).isEqualTo("1");
        assertThat(wrappedRequest.getHeader("X-User-Email")).isEqualTo("test@example.com");
        assertThat(wrappedRequest.getHeader("X-User-Role")).isEqualTo("HR");
    }

    @Test
    void doFilterInternal_NoAuthentication_NoHeaders() throws ServletException, IOException {

        SecurityContextHolder.clearContext();

        addUserHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AnonymousUser_NoHeaders() throws ServletException, IOException {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymous", null)
        );

        addUserHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Получение всех заголовков через getHeaderNames")
    void doFilterInternal_GetHeaderNames_IncludesCustomHeaders() throws ServletException, IOException {

        UserPrincipal userPrincipal = new UserPrincipal(
                2L,
                "admin@example.com",
                "ADMIN",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
        );

        // мокаем getHeaderNames для запроса
        Vector<String> originalHeaderNames = new Vector<>();
        originalHeaderNames.add("Content-Type");
        originalHeaderNames.add("Accept");
        when(request.getHeaderNames()).thenReturn(originalHeaderNames.elements());

        addUserHeadersFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), any());

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        List<String> headerNamesList = Collections.list(wrappedRequest.getHeaderNames());

        assertThat(headerNamesList)
                .contains("X-User-Id", "X-User-Email", "X-User-Role")
                .contains("Content-Type", "Accept");
    }

    @Test
    void doFilterInternal_GetHeaders_ReturnsEnumeration() throws ServletException, IOException {

        UserPrincipal userPrincipal = new UserPrincipal(
                3L,
                "hr@example.com",
                "HR",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities())
        );

        addUserHeadersFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), any());

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(Collections.list(wrappedRequest.getHeaders("X-User-Id")))
                .containsExactly("3");
        assertThat(Collections.list(wrappedRequest.getHeaders("X-User-Email")))
                .containsExactly("hr@example.com");
        assertThat(Collections.list(wrappedRequest.getHeaders("X-User-Role")))
                .containsExactly("HR");
    }
}
