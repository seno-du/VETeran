package com.jjangtrio.veteran.ServerApplication.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple Broker 설정 (client에서 /topic으로 시작하는 메시지 구독 가능)
        config.enableSimpleBroker("/topic");
        // Application Prefix 설정 (/app으로 시작하는 메시지를 컨트롤러가 처리)
        config.setApplicationDestinationPrefixes("/app");
    }

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.info("WebSocket 엔드포인트 등록됨: /ws");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns( "http://localhost:6100", "http://localhost:5000")
                .withSockJS();
    }
}
