package com.github.wreulicke.spring.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionSystemException;

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
	public void testCreate1() {
		assertThatThrownBy(controller::create1)
			.isInstanceOf(TransactionSystemException.class);
		
		List<User> users = manager.createQuery("select u from User u", User.class).getResultList();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testA"))
			.isEmpty();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testB"))
			.isEmpty();
	}
	
	@Test
	public void testCreate2() {
		User user = controller.create2();
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
	
	@Test
	public void testCreate3() {
		controller.create3();
		List<User> users = manager.createQuery("select u from User u", User.class).getResultList();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testE"))
			.isEmpty();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testF"))
			.isNotEmpty();
	}
	
	@Test
	public void testCreate4() {
		assertThatThrownBy(controller::create4)
			.isInstanceOf(RuntimeException.class);
		List<User> users = manager.createQuery("select u from User u", User.class).getResultList();
		assertThat(users)
			.filteredOn(u -> u.getName().equals("testG"))
			.isNotEmpty();
	}
}
