package com.github.wreulicke.spring;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

@ServerEndpoint("/test")
public class TestEndpoint {
  
  @OnMessage
  public void test(String message, Session session) throws IOException {
    WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
    System.out.println(context);
    
    session.getBasicRemote()
      .sendText("test "+message);
  }
  
  @OnError
  public void onError(Session session, Throwable throwable) {
  }
  

  @OnClose
  public void onClose(Session session, CloseReason closeReason) throws IOException {
  }

}

