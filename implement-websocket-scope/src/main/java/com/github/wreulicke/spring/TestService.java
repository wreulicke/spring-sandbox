package com.github.wreulicke.spring;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = WebSocketConfig.WEB_SOCKET_SCOPE_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TestService {
  public String serve() {
    return this.toString();
  }
}

