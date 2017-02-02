package com.github.wreulicke.spring;

import javax.servlet.ServletContext;

import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[] {
      WebSocketConfig.class
    };
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {};
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] {
      "/echo"
    };
  }
  @Override
  protected void registerContextLoaderListener(ServletContext servletContext) {
    super.registerContextLoaderListener(servletContext);
    servletContext.addListener(new RequestContextListener());
  }
}
