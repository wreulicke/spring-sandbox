package com.github.wreulicke.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class WebSocketScope implements Scope {

  @Override
  public Object get(String name, ObjectFactory<?> factory) {
    return WebSocketSessionHolder.getCurrentContainer()
      .get(name, factory::getObject);
  }

  @Override
  public String getConversationId() {
    return WebSocketSessionHolder.current()
      .getId();
  }

  @Override
  public void registerDestructionCallback(String name, Runnable runnable) {
    WebSocketSessionHolder.registerDestructionCallback(name, runnable);
  }

  @Override
  public Object remove(String name) {
    WebSocketScopeContainer container = WebSocketSessionHolder.getCurrentContainer();
    Object object = container.get(name);
    if (object == null) {
      return null;
    }
    else {
      container.remove(name);
      return object;
    }
  }

  @Override
  public Object resolveContextualObject(String name) {
    return null;
  }

}
