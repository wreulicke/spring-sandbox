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

import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wreulicke.simple.user.CreateUserRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabaseConfiguration.class)
public class SimpleApplicationTests {

  @Autowired
  TestRestTemplate template;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  public void createUser() throws JsonProcessingException {
    HttpHeaders headers = new HttpHeaders();
    byte[] encoded = java.util.Base64.getEncoder()
      .encode("admin:admin".getBytes(StandardCharsets.UTF_8));
    headers.add(HttpHeaders.AUTHORIZATION, "Basic " + new String(encoded, StandardCharsets.UTF_8));

    CreateUserRequest request = new CreateUserRequest("test", "test");
    HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

    ResponseEntity<String> responseEntity = template.postForEntity("/users", entity, String.class);
    String value = responseEntity.getHeaders()
      .get("Set-Cookie")
      .iterator()
      .next();
    Optional<HttpCookie> cookieOpt = HttpCookie.parse(value)
      .stream()
      .filter(cookie -> cookie.getName()
        .equals("XSRF-TOKEN"))
      .findFirst();
      headers.add("Cookie", "XSRF-TOKEN=" + cookieOpt.orElseThrow(RuntimeException::new)
        .getValue());
      headers.add("X-XSRF-TOKEN", cookieOpt.orElseThrow(RuntimeException::new)
          .getValue());

    responseEntity = template.postForEntity("/users", new HttpEntity<>(request, headers), String.class);
    assertThat(responseEntity).returns(HttpStatus.OK, ResponseEntity::getStatusCode);
  }
}
