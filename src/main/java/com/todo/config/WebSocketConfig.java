package com.todo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfig implements WebSocketConfigurer {
    // private final RoomChatWebSocketHandler roomChatWebSocketHandler;
    //
    // public WebSocketConfig(RoomChatWebSocketHandler roomChatWebSocketHandler) {
    //     this.roomChatWebSocketHandler = roomChatWebSocketHandler;
    // }

    /**
     * 注入ServerEndpointExporter，
     * 这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
     */
    // @Bean
    // public ServerEndpointExporter serverEndpointExporter() {
    //     return new ServerEndpointExporter();
    // }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // registry.addHandler(roomChatWebSocketHandler, "/roomchat")
        //         .addInterceptors(new WebSocketInterceptor())
        //         .setAllowedOrigins("*");
    }

}
