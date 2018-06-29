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

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import com.github.wreulicke.simple.user.CustomUserDetails;
import com.github.wreulicke.simple.user.User;
import com.github.wreulicke.simple.user.UserAuthorities;
import com.github.wreulicke.simple.user.UserAuthoritiesRepository;
import com.github.wreulicke.simple.user.UserRepository;

@RequiredArgsConstructor
@Controller
public class SignupController {

  private final UserRepository userRepository;

  private final UserAuthoritiesRepository userAuthoritiesRepository;

  @Autowired(required = false)
  ProviderSignInUtils providerSignInUtils;

  private final PasswordEncoder encoder;

  private final PlatformTransactionManager platformTransactionManager;

  @RequestMapping(value = "/signup", method = RequestMethod.GET)
  public SignupForm signupForm(WebRequest webRequest) {
    SignupForm signupForm = new SignupForm();
    if (providerSignInUtils != null) {
      Connection<?> connection = providerSignInUtils.getConnectionFromSession(webRequest);
      UserProfile userProfile = connection.fetchUserProfile();
      signupForm.setUsername(userProfile.getUsername());
    }

    return signupForm;
  }

  @RequestMapping(value = "/signup", method = RequestMethod.POST)
  public String signup(@Valid SignupForm signupForm, BindingResult formBinding, WebRequest webRequest) {
    if (formBinding.hasErrors()) {
      return null;
    }

    UserDetails user = new TransactionTemplate(platformTransactionManager).execute(attr -> createUser(signupForm));
    Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
    SecurityContextHolder.getContext()
      .setAuthentication(authentication);
    if (providerSignInUtils != null) {
      providerSignInUtils.doPostSignUp(user.getUsername(), webRequest);
    }
    return "redirect:/health";
  }

  private UserDetails createUser(SignupForm signupRequest) {
    User user = new User();
    user.setUsername(signupRequest.getUsername());
    user.setPassword(encoder.encode(signupRequest.getPassword()));
    userRepository.save(user);
    UserAuthorities authorities = new UserAuthorities();
    authorities.setUsername(signupRequest.getUsername());
    authorities.setAuthorities(Collections.singleton("ADMIN"));
    userAuthoritiesRepository.save(authorities);
    return new CustomUserDetails(user, authorities);
  }
}
