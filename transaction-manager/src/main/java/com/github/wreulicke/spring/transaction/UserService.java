package com.github.wreulicke.spring.transaction;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	
	@Autowired
	EntityManager entityManager;
	
	
	@Transactional
	public User create(User user) {
		entityManager.persist(user);
		return user;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public User tryCreateInNewTransaction(User user) {
		entityManager.persist(user);
		throw new RuntimeException();
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public User createInNewTransaction(User user) {
		entityManager.persist(user);
		return user;
	}
}
