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

import java.util.Collections;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;

  private final UserAuthoritiesRepository userAuthoritiesRepository;

  private final PasswordEncoder encoder;

  private final PlatformTransactionManager transactionManager;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> create(@RequestBody CreateUserRequest request) {
    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(encoder.encode(request.getPassword()));

    try {
      return new TransactionTemplate(transactionManager).execute(status -> {
        userRepository.save(user);
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setUsername(user.getUsername());
        userAuthorities.setAuthorities(request.getAuthorities());
        userAuthoritiesRepository.save(userAuthorities);
        return ResponseEntity.ok(new UserResponse(user, userAuthorities));
      });
    } catch (DataIntegrityViolationException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .build();
    }
  }


  @DeleteMapping("/{username}")
  @Transactional
  public ResponseEntity<?> delete(@PathVariable("username") String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (!userOpt.isPresent()) {
      return ResponseEntity.notFound()
        .build();
    }

    User user = userOpt.get();
    userRepository.delete(user);
    UserAuthorities userAuthorities = userAuthoritiesRepository.findByUsername(username)
      .orElse(null);
    return ResponseEntity.ok(new UserResponse(user, userAuthorities));
  }


  @PostMapping(path = "/{username}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> update(@PathVariable("username") String username, @RequestBody UpdateUserRequest request) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (!userOpt.isPresent()) {
      return ResponseEntity.notFound()
        .build();
    }

    User user = userOpt.get();

    try {
      new TransactionTemplate(transactionManager).execute(status -> {
        // TODO: イケテナイ
        // 何がイケテナイかというと
        // userAuthoritiesが存在しない場合にUpdate時に勝手に作られてしまう。
        // デフォルトは"USER"なのに対して、ここでは空にしている。
        // １個のEntityの方がよかったかもね
        UserAuthorities userAuthorities = userAuthoritiesRepository.findByUsername(user.getUsername())
          .orElseGet(() -> {
            UserAuthorities authorities = new UserAuthorities();
            authorities.setUsername(user.getUsername());
            authorities.setAuthorities(Collections.emptySet());
            return userAuthoritiesRepository.save(authorities);
          });
        request.update(user, userAuthorities, encoder);
        return userRepository.save(user);
      });
    } catch (DataIntegrityViolationException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .build();
    }

    UserAuthorities userAuthorities = userAuthoritiesRepository.findByUsername(username)
      .orElse(null);
    return ResponseEntity.ok(new UserResponse(user, userAuthorities));
  }


  @GetMapping(path = "/{username}")
  @Transactional
  public ResponseEntity<?> get(@PathVariable("username") String username) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (!userOpt.isPresent()) {
      return ResponseEntity.notFound()
        .build();
    }

    User user = userOpt.get();
    UserAuthorities userAuthorities = userAuthoritiesRepository.findByUsername(username)
      .orElse(null);
    return ResponseEntity.ok(new UserResponse(user, userAuthorities));
  }

}
