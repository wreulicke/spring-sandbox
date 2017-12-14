package com.github.wreulicke.spring.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {
	
	@Autowired
	UserController controller;
	
	
	@Test
	public void test() {
		User user = controller.create();
		assertThat(user)
			.isNotNull();
		assertThat(user.getId())
			.isNotNull();
	}
	
	@Test
	public void testCreateNew() {
		User user = controller.createInNewTransaction();
		assertThat(user)
			.isNotNull();
		assertThat(user.getId())
			.isNotNull();
	}
}
