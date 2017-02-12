package com.github.wreulicke.spring;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.core.NamedThreadLocal;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketSessionHolder {
  private static ThreadLocal<WebSocketSession> session = new NamedThreadLocal<>("WebScoketThreadLocal");
  private static ConcurrentMap<WebSocketSession, WebSocketScopeContainer> pool = new ConcurrentHashMap<>();

  public static WebSocketSession setSession(WebSocketSession session) {
    WebSocketSessionHolder.session.set(session);
    pool.putIfAbsent(session, new WebSocketScopeContainer());
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

  public static void removeSession(WebSocketSession session) {
    WebSocketScopeContainer container = pool.get(session);
    if (container != null) {
      container.executeDestructionCallback();
      pool.remove(session);
    }
  }

  public static void registerDestructionCallback(String name, Runnable runnable) {
    getCurrentContainer().registerDestructionCallback(name, runnable);
  }

  public static void completeSession() {
    getCurrentContainer().executeDestructionCallback();
  }
}
