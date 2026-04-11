package ru.devanalyzer.gateway_service.security;

import java.security.Principal;

public class WebSocketUserPrincipal implements Principal {
    
    private final Long userId;
    private final String email;
    
    public WebSocketUserPrincipal(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }
    
    @Override
    public String getName() {
        return userId.toString();
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getEmail() {
        return email;
    }
}
