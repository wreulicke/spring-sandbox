package com.github.wreulicke.simple.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {
	
	@Autowired
	UserRepository repository;
	
	@Test
	public void test() {
		User user = new User();
		User actual = repository.save(user);
		assertThat(actual.getUsername()).isNotEmpty();
	}
	
}
