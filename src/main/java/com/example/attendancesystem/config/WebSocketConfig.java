package com.example.attendancesystem.config;

import com.example.attendancesystem.service.RfidWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final RfidWebSocketHandler rfidWebSocketHandler;

    public WebSocketConfig(RfidWebSocketHandler rfidWebSocketHandler) {
        this.rfidWebSocketHandler = rfidWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(rfidWebSocketHandler, "/ws")
                .setAllowedOrigins("http://localhost:4200"); // Allow Angular frontend
    }
}
