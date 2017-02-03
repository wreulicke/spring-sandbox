package com.github.wreulicke.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WebSocketScopeContainer {
  private Map<String, Object> pool = new HashMap<>();

  public Object remove(String name) {
    return pool.remove(name);
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

}
