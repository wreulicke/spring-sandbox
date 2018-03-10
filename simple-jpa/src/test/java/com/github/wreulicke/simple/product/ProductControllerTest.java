package com.github.wreulicke.simple.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wreulicke.simple.test.ControllerTest;
import com.github.wreulicke.simple.test.WithAdmin;

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
      .andExpect(status().isOk());

  }
}
