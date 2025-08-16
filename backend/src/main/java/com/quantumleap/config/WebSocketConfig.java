package com.quantumleap.config;

import com.quantumleap.ws.WhiteboardWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import jakarta.annotation.PostConstruct;

/**
 * WebSocket configuration for real-time whiteboard collaboration
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final WhiteboardWebSocketHandler whiteboardWebSocketHandler;

    @PostConstruct
    public void init() {
        log.info("[WS Config] WebSocketConfig initialized successfully");
        log.info("[WS Config] WhiteboardWebSocketHandler: {}", whiteboardWebSocketHandler != null ? "OK" : "NULL");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("[WS Config] Starting to register WebSocket handlers...");
        
        // Register WebSocket handler at the full path including context path
        // Backend context path: /api/v1
        // Frontend connects to: ws://localhost:8080/api/v1/ws/whiteboard/{boardId}
        // So we need to register at: /api/v1/ws/whiteboard/{boardId}
        log.info("[WS Config] Registering WebSocket handler at /api/v1/ws/whiteboard/{boardId}");
        
        // Try both paths to see which one works
        registry.addHandler(whiteboardWebSocketHandler, "/api/v1/ws/whiteboard/{boardId}")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        
        // Also register at root path as fallback
        registry.addHandler(whiteboardWebSocketHandler, "/ws/whiteboard/{boardId}")
                .setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
        
        log.info("[WS Config] WebSocket handlers registered successfully");
        log.info("[WS Config] Registry: {}", registry);
    }
}
