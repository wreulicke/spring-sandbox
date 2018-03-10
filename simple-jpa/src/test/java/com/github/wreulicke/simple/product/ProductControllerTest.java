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
package com.github.wreulicke.simple.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wreulicke.simple.test.ControllerTest;
import com.github.wreulicke.simple.test.WithAdmin;
import com.jayway.jsonpath.JsonPath;

@ControllerTest
@RunWith(SpringRunner.class)
public class ProductControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper mapper;

  @Test
  @Transactional
  @WithAdmin
  public void test() throws Exception {
    CreateProductRequest request = new CreateProductRequest("test", Optional.empty());

    mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("description").isNotEmpty())
      .andExpect(jsonPath("count").value(0));
  }

  @Test
  @Transactional
  @WithAdmin
  public void testUpdate() throws Exception {
    CreateProductRequest request = new CreateProductRequest("test", Optional.empty());

    String content = mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("description").isNotEmpty())
      .andExpect(jsonPath("count").value(0))
      .andReturn()
      .getResponse()
      .getContentAsString();

    Number id = JsonPath.read(content, "$.id");

    UpdateProductRequest updateProductRequest = new UpdateProductRequest(Optional.of("modified"), Optional.of(10L));

    mvc.perform(post("/products/{productId}", id).contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(updateProductRequest)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("description").value("modified"))
      .andExpect(jsonPath("count").value(10));


  }

  @Test
  @Transactional
  @WithAdmin
  public void testGet() throws Exception {
    CreateProductRequest request = new CreateProductRequest("test", Optional.empty());

    String content = mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
      .content(mapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("description").isNotEmpty())
      .andExpect(jsonPath("count").value(0))
      .andReturn()
      .getResponse()
      .getContentAsString();

    Number id = JsonPath.read(content, "$.id");

    mvc.perform(get("/products/{productId}", id))
      .andExpect(status().isOk())
      .andExpect(jsonPath("id").isNotEmpty())
      .andExpect(jsonPath("description").value("test"))
      .andExpect(jsonPath("count").value(0));


  }
}
