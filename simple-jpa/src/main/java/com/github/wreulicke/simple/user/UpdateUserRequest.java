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
package com.github.wreulicke.simple.user;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@ToString(exclude = "password")
@EqualsAndHashCode
@Getter
public class UpdateUserRequest {

  private final Optional<String> username;

  private final Optional<String> password;

  private final Optional<Set<String>> authorities;

  public UpdateUserRequest(@NonNull Optional<String> username, @NonNull Optional<String> password) {
    this.username = username;
    this.password = password;
    this.authorities = Optional.empty();
  }

  @JsonCreator
  public UpdateUserRequest(@JsonProperty("username") @NonNull Optional<String> username, @JsonProperty("password") @NonNull Optional<String> password,
    @JsonProperty("authorities") @NonNull Optional<Set<String>> authorities) {
    this.username = username;
    this.password = password;
    this.authorities = authorities;
  }


  public void update(User user, UserAuthorities userAuthorities, PasswordEncoder encoder) {
    username.ifPresent(user::setUsername);
    password.ifPresent(pass -> user.setPassword(encoder.encode(pass)));
    authorities.ifPresent(userAuthorities::setAuthorities);
  }
}
