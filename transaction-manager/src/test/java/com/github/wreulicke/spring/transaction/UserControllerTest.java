package com.github.wreulicke.spring.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

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
	
	@Autowired
	EntityManager manager;
	
	
	@Test
	public void test() {
		User user = controller.create();
		assertThat(user)
			.isNotNull();
		assertThat(user.getId())
			.isNotNull();
		List<User> users = manager.createQuery("select * from User u", User.class).getResultList();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testA"))
			.isEmpty();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testB"))
			.isEmpty();
	}
	
	@Test
	public void testCreateInNewTransaction() {
		User user = controller.createInNewTransaction();
		assertThat(user)
			.isNotNull();
		assertThat(user.getId())
			.isNotNull();
		List<User> users = manager.createQuery("select u from User u", User.class).getResultList();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testC"))
			.isEmpty();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testD"))
			.isNotEmpty();
	}
}
