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
package com.github.wreulicke.simple;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.social.security.SpringSocialConfigurer;
import org.springframework.web.context.request.NativeWebRequest;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf()
      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

    http.authorizeRequests()
      .antMatchers("/login")
      .permitAll()
      .antMatchers("/auth/*")
      .permitAll()
      .antMatchers("/signup")
      .permitAll()
      .anyRequest()
      .authenticated()
      .and()
      .httpBasic()
      .realmName("clients")
      .and()
      .formLogin()
      .loginProcessingUrl("/login")
      .loginPage("/login")
      .defaultSuccessUrl("/")
      .and()
      .logout()
      .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
      .logoutSuccessUrl("/login")
      .and()
      .apply(new SpringSocialConfigurer());
  }

  @Bean
  public PasswordEncoder bcrpt() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ProviderSignInUtils providerSignInUtils(ConnectionFactoryLocator locator, UsersConnectionRepository repo) {
    return new ProviderSignInUtils(locator, repo);
  }

  @Bean
  public SignInAdapter signInAdapter() {
    return new SimpleSignInAdapter(new HttpSessionRequestCache());
  }

  @RequiredArgsConstructor
  public class SimpleSignInAdapter implements SignInAdapter {

    private final RequestCache requestCache;

    @Override
    public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
      Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, null);
      SecurityContextHolder.getContext()
        .setAuthentication(authentication);
      return extractOriginalUrl(request);
    }

    private String extractOriginalUrl(NativeWebRequest request) {
      HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
      HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
      SavedRequest saved = requestCache.getRequest(nativeReq, nativeRes);
      if (saved == null) {
        return null;
      }
      requestCache.removeRequest(nativeReq, nativeRes);
      removeAuthenticationAttributes(nativeReq.getSession(false));
      return saved.getRedirectUrl();
    }

    private void removeAuthenticationAttributes(HttpSession session) {
      if (session == null) {
        return;
      }
      session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
  }
}
