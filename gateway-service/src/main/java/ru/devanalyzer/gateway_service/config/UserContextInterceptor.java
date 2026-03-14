package ru.devanalyzer.gateway_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.devanalyzer.gateway_service.security.UserPrincipal;

import java.io.IOException;

@Slf4j
@Component
public class UserContextInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            request.getHeaders().add("X-User-Id", String.valueOf(userPrincipal.getUserId()));
            request.getHeaders().add("X-User-Email", userPrincipal.getEmail());
            request.getHeaders().add("X-User-Role", userPrincipal.getRole());
            
            log.debug("Added user context headers: userId={}, email={}, role={}", 
                    userPrincipal.getUserId(), userPrincipal.getEmail(), userPrincipal.getRole());
        }
        
        return execution.execute(request, body);
    }
}
