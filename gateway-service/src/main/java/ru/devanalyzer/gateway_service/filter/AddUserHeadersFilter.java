package ru.devanalyzer.gateway_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.devanalyzer.gateway_service.security.UserPrincipal;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AddUserHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("AddUserHeadersFilter triggered for path: {}", request.getRequestURI());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        log.info("Authentication: {}", auth != null ? auth.getClass().getSimpleName() : "null");
        if (auth != null) {
            log.info("Principal: {}", auth.getPrincipal().getClass().getSimpleName());
        }

        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    if ("X-User-Id".equals(name)) {
                        return userPrincipal.getUserId().toString();
                    } else if ("X-User-Email".equals(name)) {
                        return userPrincipal.getEmail();
                    } else if ("X-User-Role".equals(name)) {
                        return userPrincipal.getRole();
                    }
                    return super.getHeader(name);
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if ("X-User-Id".equals(name)) {
                        return Collections.enumeration(List.of(userPrincipal.getUserId().toString()));
                    } else if ("X-User-Email".equals(name)) {
                        return Collections.enumeration(List.of(userPrincipal.getEmail()));
                    } else if ("X-User-Role".equals(name)) {
                        return Collections.enumeration(List.of(userPrincipal.getRole()));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    Set<String> names = new HashSet<>();
                    Enumeration<String> originalNames = super.getHeaderNames();
                    while (originalNames.hasMoreElements()) {
                        names.add(originalNames.nextElement());
                    }
                    names.add("X-User-Id");
                    names.add("X-User-Email");
                    names.add("X-User-Role");
                    return Collections.enumeration(names);
                }
            };

            log.debug("Added user headers: userId={}, email={}, role={}",
                    userPrincipal.getUserId(), userPrincipal.getEmail(), userPrincipal.getRole());

            filterChain.doFilter(wrappedRequest, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
