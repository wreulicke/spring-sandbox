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

import java.util.Collections;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.wreulicke.simple.user.User;
import com.github.wreulicke.simple.user.UserAuthorities;
import com.github.wreulicke.simple.user.UserAuthoritiesRepository;
import com.github.wreulicke.simple.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
@ConfigurationProperties("wreulicke.custom.user_details")
public class InitializationProperties implements InitializingBean {

  boolean initAdmin = true;

  private final UserRepository userRepository;

  private final UserAuthoritiesRepository userAuthoritiesRepository;

  private final PasswordEncoder encoder;

  @Override
  @Transactional
  public void afterPropertiesSet() {
    if (initAdmin) {
      if (userRepository.findByUsername("admin")
        .isPresent()) {
        return;
      }

      User user = new User();
      user.setUsername("admin");
      user.setPassword(encoder.encode("admin"));
      User registered = userRepository.save(user);
      UserAuthorities authorities = new UserAuthorities();
      authorities.setUsername(user.getUsername());
      authorities.setAuthorities(Collections.singleton("ROLE_ADMIN"));
      userAuthoritiesRepository.save(authorities);

      log.info("{}", registered);
    }
  }
}
