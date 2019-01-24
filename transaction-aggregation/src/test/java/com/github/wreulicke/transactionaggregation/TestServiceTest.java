package com.github.wreulicke.transactionaggregation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TestServiceTest {
	
	@Autowired
	TestController testController;
	
	@Autowired
	TestService testService;
	
	@Test
	public void test() {
		testController.test();
		assertThat(TransactionSynchronizationManager.getResource(testService))
			.isNull();
		assertThatThrownBy(TransactionSynchronizationManager::getSynchronizations)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Transaction synchronization is not active");
	}
}
