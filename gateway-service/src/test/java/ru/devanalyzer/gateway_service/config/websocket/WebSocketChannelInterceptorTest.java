package ru.devanalyzer.gateway_service.config.websocket;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.devanalyzer.gateway_service.security.UserPrincipal;
import ru.devanalyzer.gateway_service.security.WebSocketUserPrincipal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class WebSocketChannelInterceptorTest {

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WebSocketChannelInterceptor channelInterceptor;

    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "HR",
                List.of(new SimpleGrantedAuthority("ROLE_HR"))
        );
        sessionAttributes = new HashMap<>();
        sessionAttributes.put("principal", userPrincipal);
    }

    /**
     * Создает MUTABLE сообщение с StompHeaderAccessor.
     * Использует GenericMessage с мутабельными заголовками - имитирует поведение Spring.
     */
    private Message<byte[]> createMutableMessage(StompCommand command,
                                                 Map<String, Object> sessionAttrs,
                                                 String sessionId,
                                                 String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (sessionAttrs != null) {
            accessor.setSessionAttributes(sessionAttrs);
        }
        if (sessionId != null) {
            accessor.setSessionId(sessionId);
        }
        if (destination != null) {
            accessor.setDestination(destination);
        }

        return new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void preSend_ConnectCommand_NoPrincipal_ThrowsException() {

        Message<byte[]> message = createMutableMessage(
                StompCommand.CONNECT, new HashMap<>(), "session-123", null
        );


        assertThatThrownBy(() -> channelInterceptor.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Unauthorized WebSocket connection");
    }

    @Test
    void preSend_ConnectCommand_NullAttributes_ThrowsException() {

        Message<byte[]> message = createMutableMessage(
                StompCommand.CONNECT, null, "session-123", null
        );


        assertThatThrownBy(() -> channelInterceptor.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void preSend_DisconnectCommand_Success() {

        Message<byte[]> message = createMutableMessage(
                StompCommand.DISCONNECT, null, "session-123", null
        );

        Message<?> result = channelInterceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }

    @Test
    void preSend_MessageCommand_Success() {

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        accessor.setDestination("/app/send");
        accessor.setSessionId("session-123");
        byte[] payload = "test payload".getBytes();
        Message<byte[]> message = new GenericMessage<>(payload, accessor.getMessageHeaders());

        Message<?> result = channelInterceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }

    @Test
    void preSend_CommandWithExistingUser_DoesNotOverride() {

        WebSocketUserPrincipal existingUser = new WebSocketUserPrincipal(2L, "existing@example.com");
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setSessionAttributes(sessionAttributes);
        accessor.setUser(existingUser);
        Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());

        Message<?> result = channelInterceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isEqualTo(existingUser);
        WebSocketUserPrincipal wsPrincipal = (WebSocketUserPrincipal) resultAccessor.getUser();
        Assertions.assertNotNull(wsPrincipal);
        assertThat(wsPrincipal.getUserId()).isEqualTo(2L);
        assertThat(wsPrincipal.getEmail()).isEqualTo("existing@example.com");
    }

    @Test
    void preSend_NullAccessor_ReturnsMessage() {

        Message<String> message = new GenericMessage<>("test payload");

        Message<?> result = channelInterceptor.preSend(message, channel);

        assertThat(result).isSameAs(message);
    }

    @Test
    void preSend_ErrorCommand_Success() {

        Message<byte[]> message = createMutableMessage(
                StompCommand.ERROR, null, "session-123", null
        );

        Message<?> result = channelInterceptor.preSend(message, channel);

        assertThat(result).isNotNull();
    }

}
