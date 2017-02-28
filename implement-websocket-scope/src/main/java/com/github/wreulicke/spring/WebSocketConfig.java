package com.github.wreulicke.spring;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableAspectJAutoProxy
@ComponentScan
public class WebSocketConfig implements WebSocketConfigurer {
  @Autowired
  EchoHandler echoHandler;

  public static final String WEB_SOCKET_SCOPE_NAME = "test_webscoket";

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(echoHandler, "/echo");
  }

  @Bean
  public static CustomScopeConfigurer customScopeConfigurer() {
    CustomScopeConfigurer configurer = new CustomScopeConfigurer();
    configurer.addScope(WEB_SOCKET_SCOPE_NAME, new WebSocketScope());
    return configurer;
  }
}

