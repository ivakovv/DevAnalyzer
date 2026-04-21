package ru.devanalyzer.gateway_service.config.websocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Mock
    private WebSocketAuthenticationInterceptor authenticationInterceptor;

    @Mock
    private WebSocketChannelInterceptor channelInterceptor;

    @Mock
    private MessageBrokerRegistry messageBrokerRegistry;

    @Mock
    private StompEndpointRegistry stompEndpointRegistry;

    @Mock
    private StompWebSocketEndpointRegistration endpointRegistration;

    @Mock
    private ChannelRegistration inboundChannelRegistration;

    @Mock
    private ChannelRegistration outboundChannelRegistration;

    @InjectMocks
    private WebSocketConfig webSocketConfig;

    @Test
    void configureMessageBroker_Success() {

        webSocketConfig.configureMessageBroker(messageBrokerRegistry);

        verify(messageBrokerRegistry).enableSimpleBroker("/topic", "/queue");
        verify(messageBrokerRegistry).setApplicationDestinationPrefixes("/app");
        verify(messageBrokerRegistry).setUserDestinationPrefix("/user");
    }

    @Test
    void configureClientInboundChannel_Success() {

        webSocketConfig.configureClientInboundChannel(inboundChannelRegistration);

        verify(inboundChannelRegistration).interceptors(channelInterceptor);
    }

    @Test
    void configureClientOutboundChannel_Success() {

        webSocketConfig.configureClientOutboundChannel(outboundChannelRegistration);

        verify(outboundChannelRegistration).interceptors(channelInterceptor);
    }

    @Test
    void registerStompEndpoints_Success() {
        when(stompEndpointRegistry.addEndpoint("/dev-analyzer"))
                .thenReturn(endpointRegistration);
        when(endpointRegistration.setAllowedOriginPatterns("*"))
                .thenReturn(endpointRegistration);
        when(endpointRegistration.addInterceptors(authenticationInterceptor))
                .thenReturn(endpointRegistration);

        webSocketConfig.registerStompEndpoints(stompEndpointRegistry);

        verify(stompEndpointRegistry).addEndpoint("/dev-analyzer");
        verify(endpointRegistration).setAllowedOriginPatterns("*");
        verify(endpointRegistration).addInterceptors(authenticationInterceptor);
        verify(endpointRegistration).withSockJS();
    }
}
