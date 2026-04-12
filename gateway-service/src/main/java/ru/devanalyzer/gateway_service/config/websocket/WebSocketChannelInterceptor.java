package ru.devanalyzer.gateway_service.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import ru.devanalyzer.gateway_service.security.UserPrincipal;
import ru.devanalyzer.gateway_service.security.WebSocketUserPrincipal;

@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case DISCONNECT -> handleDisconnect(accessor);
            case MESSAGE -> handleMessage(accessor, message);
            default -> {
                restoreUserFromSession(accessor);
            }
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        Object principalObj = accessor.getSessionAttributes() != null
            ? accessor.getSessionAttributes().get("principal")
            : null;

        if (principalObj instanceof UserPrincipal userPrincipal) {
            WebSocketUserPrincipal wsPrincipal = new WebSocketUserPrincipal(
                userPrincipal.getUserId(),
                userPrincipal.getEmail()
            );
            accessor.setUser(wsPrincipal);
            log.info("WebSocket CONNECT: userId={}, email={}, sessionId={}",
                userPrincipal.getUserId(), userPrincipal.getEmail(), accessor.getSessionId());
        } else {
            log.warn("WebSocket CONNECT rejected: no principal, sessionId={}", accessor.getSessionId());
            throw new AccessDeniedException("Unauthorized WebSocket connection");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        restoreUserFromSession(accessor);
        log.info("WebSocket SUBSCRIBE: destination={}, user={}, sessionId={}",
            accessor.getDestination(),
            accessor.getUser() != null ? accessor.getUser().getName() : "null",
            accessor.getSessionId());
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        log.info("WebSocket DISCONNECT: user={}, sessionId={}",
            accessor.getUser() != null ? accessor.getUser().getName() : "null",
            accessor.getSessionId());
    }

    private void handleMessage(StompHeaderAccessor accessor, Message<?> message) {
        log.info("WebSocket OUTBOUND MESSAGE: destination={}, user={}, sessionId={}, payload={}",
            accessor.getDestination(),
            accessor.getUser() != null ? accessor.getUser().getName() : "null",
            accessor.getSessionId(),
            message.getPayload());
    }

    private void restoreUserFromSession(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null && accessor.getSessionAttributes() != null) {
            Object principalObj = accessor.getSessionAttributes().get("principal");
            if (principalObj instanceof UserPrincipal userPrincipal) {
                WebSocketUserPrincipal wsPrincipal = new WebSocketUserPrincipal(
                    userPrincipal.getUserId(),
                    userPrincipal.getEmail()
                );
                accessor.setUser(wsPrincipal);
                log.debug("WebSocket: restored user userId={} for command={}",
                    userPrincipal.getUserId(), accessor.getCommand());
            }
        }
    }
}
