package com.github.wreulicke.spring;


import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

@Configuration
@EnableWebSocket
@EnableAspectJAutoProxy
@ComponentScan
public class WebSocketConfig implements WebSocketConfigurer {
  
  public static final String WEB_SOCKET_SCOPE_NAME = "test_webscoket";

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(new PerConnectionWebSocketHandler(EchoHandler.class), "/echo");
  }
  
  @Bean 
  public CustomScopeConfigurer customScopeConfigurer(){
    CustomScopeConfigurer configurer = new CustomScopeConfigurer();
    configurer.addScope(WEB_SOCKET_SCOPE_NAME, new WebSocketScope());
    return configurer;
  }
}

