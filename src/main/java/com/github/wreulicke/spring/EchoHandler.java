package com.github.wreulicke.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class EchoHandler extends TextWebSocketHandler {

  private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.put(session.getId(), session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session.getId());
    SocketSessionHolder.removeSession(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    SocketSessionHolder.setSession(session);
    try {
      WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
      System.out.println(context.getBean(TestService.class)
        .serve());
      sessions.forEach((__, other) -> {
        try {
          other.sendMessage(new TextMessage("catch " + message.getPayload()));
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    } finally {
      SocketSessionHolder.removeContext();
    }
  }
}
