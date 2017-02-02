package com.github.wreulicke.spring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.NamedThreadLocal;
import org.springframework.web.socket.WebSocketSession;

public class SocketSessionHolder {
  private static ThreadLocal<WebSocketSession> session = new NamedThreadLocal<>("WebScoketThreadLocal");
  // TODO ConcurrentMap?
  private static Map<WebSocketSession, WebSocketScopeContainer> pool = new HashMap<>();

  public static WebSocketSession setSession(WebSocketSession session) {
    SocketSessionHolder.session.set(session);
    if (pool.get(session) == null) {
      pool.put(session, new WebSocketScopeContainer());
    }
    return session;
  }

  public static WebSocketSession current() {
    return session.get();
  }

  public static WebSocketScopeContainer getCurrentContainer() {
    return pool.get(current());
  }

  public static void removeContext() {
    session.remove();
  }
  
  public static void removeSession(WebSocketSession session){
    pool.remove(session);
  }
}
