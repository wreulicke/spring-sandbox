/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.AbstractConfigurableEmbeddedServletContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class TestContextCustomizer implements ContextCustomizer {

  @Override
  public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedContextConfiguration) {
    SpringBootTest annotation = AnnotatedElementUtils.getMergedAnnotation(mergedContextConfiguration.getTestClass(), SpringBootTest.class);
    if (annotation.webEnvironment()
      .isEmbedded()) {
      ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
      if (beanFactory instanceof BeanDefinitionRegistry) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;
        registry.registerBeanDefinition(Retrofit.class.getName(), new RootBeanDefinition(TestRetrofitFactory.class));
      }
    }
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }
    return true;
  }


  public static class TestRetrofitFactory implements FactoryBean<Retrofit>, ApplicationContextAware {

    RelaxedPropertyResolver resolver;

    Environment env;

    boolean isSsl;


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
      env = context.getEnvironment();
      resolver = new RelaxedPropertyResolver(env, "server.");
      isSsl = isSslEnabled(context);
    }

    private boolean isSslEnabled(ApplicationContext context) {
      try {
        AbstractConfigurableEmbeddedServletContainer container = context.getBean(AbstractConfigurableEmbeddedServletContainer.class);
        return container.getSsl() != null && container.getSsl()
          .isEnabled();
      } catch (NoSuchBeanDefinitionException ex) {
        return false;
      }
    }

    @Override
    public Retrofit getObject() throws Exception {
      String port = this.env.getProperty("local.server.port", "8080");
      String contextPath = this.resolver.getProperty("context-path", "");
      return new Retrofit.Builder().baseUrl((isSsl ? "https" : "http") + "://localhost:" + port + contextPath)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    }

    @Override
    public Class<?> getObjectType() {
      return Retrofit.class;
    }

    @Override
    public boolean isSingleton() {
      return true;
    }
  }

}
