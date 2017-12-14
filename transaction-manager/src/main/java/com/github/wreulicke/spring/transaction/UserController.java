package com.github.wreulicke.spring.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Controller
public class UserController {
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	UserService userService;
	
	
	@Transactional
	public User create() {
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
	public User createInNewTransaction() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("SomeTxName");
		TransactionTemplate template = new TransactionTemplate(transactionManager, def);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		try {
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					User user = new User();
					user.setName("testC");
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
	
}
