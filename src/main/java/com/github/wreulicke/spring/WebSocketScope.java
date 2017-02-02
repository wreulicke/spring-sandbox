package com.github.wreulicke.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class WebSocketScope implements Scope {

  @Override
  public Object get(String name, ObjectFactory<?> factory) {
    return SocketSessionHolder.getCurrentContainer().get(name, factory::getObject);
  }

  @Override
  public String getConversationId() {
    return SocketSessionHolder.current().getId();
  }

  @Override
  public void registerDestructionCallback(String name, Runnable arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object remove(String name) {
    return SocketSessionHolder.getCurrentContainer().remove(name);
  }

  @Override
  public Object resolveContextualObject(String name) {
    // TODO Auto-generated method stub
    return null;
  }

}
