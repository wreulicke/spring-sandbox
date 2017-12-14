package com.github.wreulicke.spring.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Controller
public class UserController {
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	UserService userService;
	
	
	@Transactional
	public User create1() {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		
		try {
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					User user = new User();
					user.setName("testA");
					userService.create(user);
					throw new RuntimeException();
				}
			});
		} catch (Exception e) {
		}
		
		User user = new User();
		user.setName("testB");
		userService.create(user);
		return user;
	}
	
	@Transactional
	public User create2() {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		try {
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					User user = new User();
					user.setName("testC");
					userService.create(user);
					throw new RuntimeException();
				}
			});
		} catch (Exception e) {
		}
		
		User user = new User();
		user.setName("testD");
		userService.create(user);
		return user;
	}
	
	@Transactional
	public User create3() {
		User user1 = new User();
		user1.setName("testE");
		try {
			userService.tryCreateInNewTransaction(user1);
		} catch (Exception e) {
		}
		User user2 = new User();
		user2.setName("testF");
		userService.create(user2);
		return user2;
	}
	
	@Transactional
	public User create4() {
		User user1 = new User();
		user1.setName("testG");
		userService.createInNewTransaction(user1);
		throw new RuntimeException();
	}
}
