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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wreulicke.simple.test.ControllerTest;

@ControllerTest
@RunWith(SpringRunner.class)
public class UserControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper mapper;

  @Test
  @Transactional
  @WithMockUser(roles = "ADMIN")
  public void testCreate() throws Exception {
    CreateUserRequest request = new CreateUserRequest("test", "hogehoge");
    mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("username").value("test"))
      .andExpect(jsonPath("password").doesNotExist());
  }

  @Test
  @Transactional
  @WithMockUser(roles = "ADMIN")
  public void testDelete() throws Exception {
    CreateUserRequest request = new CreateUserRequest("test", "hogehoge");
    mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("username").value("test"))
      .andExpect(jsonPath("password").doesNotExist());


    mvc.perform(delete("/users/{username}", "test"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("username").value("test"))
      .andExpect(jsonPath("password").doesNotExist());
  }


  @Test
  @Transactional
  @WithMockUser(roles = "ADMIN")
  public void testUpdate() throws Exception {
    CreateUserRequest request = new CreateUserRequest("test", "hogehoge");
    mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("username").value("test"))
      .andExpect(jsonPath("password").doesNotExist());

    UpdateUserRequest updateUserRequest = new UpdateUserRequest(Optional.of("test2"), Optional.empty());

    mvc.perform(post("/users/{username}", "test").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(updateUserRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("username").value("test2"))
      .andExpect(jsonPath("password").doesNotExist());

    mvc.perform(get("/users/{username}", "test2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("username").value("test2"))
      .andExpect(jsonPath("password").doesNotExist());
  }

  @Test
  // JPA取り扱い難しい。トランザクション境界の話があるので、ITでやる。あとh2で動かねぇ
  @WithMockUser(roles = "ADMIN")
  public void testConflictedUpdate() throws Exception {
    try {
      CreateUserRequest request = new CreateUserRequest("test", "hogehoge");
      mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("username").value("test"))
        .andExpect(jsonPath("password").doesNotExist());

      CreateUserRequest request2 = new CreateUserRequest("test2", "hogehoge");
      mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(request2)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("username").value("test2"))
        .andExpect(jsonPath("password").doesNotExist());

      UpdateUserRequest updateUserRequest = new UpdateUserRequest(Optional.of("test2"), Optional.empty());

      mvc.perform(post("/users/{username}", "test").contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(updateUserRequest)))
        .andExpect(status().isConflict());
    } finally {
      mvc.perform(delete("/users/{username}", "test"));
      mvc.perform(delete("/users/{username}", "test2"));
    }
  }
}
