package ru.devanalyzer.user_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.devanalyzer.user_service.security.UserPrincipal;

import java.io.IOException;

@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        if (userId != null && email != null && role != null) {
            UserPrincipal userPrincipal = new UserPrincipal(
                    Long.parseLong(userId),
                    email,
                    role
            );

            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.getAuthorities()
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("User context set from headers: userId={}, email={}, role={}", 
                userId, email, role);
        }

        filterChain.doFilter(request, response);
    }
}
