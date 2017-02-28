package com.github.wreulicke.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.pmw.tinylog.Logger;

public class WebSocketScopeContainer {
  private Map<String, Object> pool = new HashMap<>();
  private Map<String, Runnable> destructionCallbackPool = new HashMap<>();
  private boolean isCompleted = false;

  public void remove(String name) {
    pool.remove(name);
    destructionCallbackPool.remove(name);
  }

  public Object get(String name, Supplier<?> supplier) {
    Object object = pool.get(name);
    if (object == null) {
      Object newObject = supplier.get();
      pool.put(name, newObject);
      return newObject;
    }
    return object;
  }

  public Object get(String name) {
    return pool.get(name);
  }

  public void registerDestructionCallback(String name, Runnable runnable) {
    destructionCallbackPool.put(name, runnable);
  }

  public void executeDestructionCallback() {
    destructionCallbackPool.forEach((key, runnable) -> {
      try {
        runnable.run();
      } catch (Throwable e) {
        Logger.error(e);
      }
    });

  }

  public boolean isCompleted() {
    return this.isCompleted;
  }

  public void completed() {
    this.isCompleted = true;
  }

}
