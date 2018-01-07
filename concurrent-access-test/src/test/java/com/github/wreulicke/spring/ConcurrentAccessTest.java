package com.github.wreulicke.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.junit.Test;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
	"security.basic.enable=false",
	"security.ignored=/**",
	"server.connection-timeout=-1",
	"spring.mvc.async.request-timeout=-1"
}, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ConcurrentAccessTest {
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@MockBean
	TestService testService;
	
	@MockBean
	FutureTestService futureTestService;
	
	
	@Test
	public void test() throws InterruptedException, ExecutionException {
		CountDownLatch latch = new CountDownLatch(11);
		doAnswer(invocation -> {
			latch.countDown();
			log.info("waiting");
			latch.await();
			return null;
		}).when(testService).doSomething();
		
		ExecutorService service = Executors.newFixedThreadPool(10);
		
		List<Future<?>> list = IntStream.range(0, 10)
			.mapToObj(ignore -> {
				log.info("submitting");
				return service.submit(() -> {
					ResponseEntity<String> entity = testRestTemplate.getForEntity("/test", String.class);
					log.info("status:{}", entity.getStatusCode());
					return null;
				});
			}).collect(Collectors.toList());
		
		ResponseEntity<String> entity = testRestTemplate.getForEntity("/health", String.class);
		assertThat(entity.getStatusCode())
			.isEqualTo(HttpStatus.OK);
		service.shutdownNow();
		service.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void test2() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(101);
		doAnswer(invocation -> {
			latch.countDown();
			log.info("waiting");
			latch.await();
			return null;
		}).when(testService).doSomething();
		
		ExecutorService service = Executors.newFixedThreadPool(101);
		
		List<Future<?>> list = IntStream.range(0, 100)
			.mapToObj(ignore -> {
				log.info("submitting");
				return service.submit(() -> {
					ResponseEntity<String> entity = testRestTemplate.getForEntity("/test", String.class);
					log.info("status:{}", entity.getStatusCode());
					return null;
				});
			}).collect(Collectors.toList());
		
		ResponseEntity<String> entity = testRestTemplate.getForEntity("/health", String.class);
		assertThat(entity.getStatusCode())
			.isEqualTo(HttpStatus.OK);
		service.shutdownNow();
		service.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void test3() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1001);
		doAnswer(invocation -> {
			latch.countDown();
			log.info("waiting");
			latch.await();
			return null;
		}).when(testService).doSomething();
		
		CountDownLatch latch2 = new CountDownLatch(1000);
		ExecutorService service = Executors.newFixedThreadPool(1000);
		
		List<Future<?>> list = IntStream.range(0, 1000)
			.mapToObj(ignore -> {
				log.info("submitting");
				return service.submit(() -> {
					latch2.countDown();
					ResponseEntity<String> entity = testRestTemplate.getForEntity("/test", String.class);
					log.info("status:{}", entity.getStatusCode());
					return null;
				});
			}).collect(Collectors.toList());
		
		latch2.await();
		assertThatThrownBy(() -> service.submit(() -> testRestTemplate.getForEntity("/health", String.class))
			.get(5, TimeUnit.SECONDS))
				.isInstanceOf(TimeoutException.class);
		service.shutdownNow();
		service.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void test4() throws InterruptedException {
		doAnswer(invocation -> {
			return new CompletableFuture<>();
		}).when(futureTestService).doSomething();
		
		CountDownLatch latch = new CountDownLatch(1000);
		ExecutorService service = Executors.newFixedThreadPool(1000);
		
		List<Future<?>> list = IntStream.range(0, 1000)
			.mapToObj(ignore -> {
				log.info("submitting");
				return service.submit(() -> {
					latch.countDown();
					ResponseEntity<String> entity = testRestTemplate.getForEntity("/future", String.class);
					log.info("status:{}", entity.getStatusCode());
					return null;
				});
			}).collect(Collectors.toList());
		
		latch.await();
		ResponseEntity<String> entity = testRestTemplate.getForEntity("/health", String.class);
		assertThat(entity.getStatusCode())
			.isEqualTo(HttpStatus.OK);
		service.shutdownNow();
		service.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	
	interface TestService {
		public void doSomething();
	}
	
	interface FutureTestService {
		public CompletableFuture<String> doSomething();
	}
	
	@RestController
	public static class TestController {
		
		public TestController() {
			log.info("created");
		}
		
		
		@Autowired
		TestService testService;
		
		@Autowired
		FutureTestService futureTestService;
		
		
		@RequestMapping("/test")
		public String test() {
			testService.doSomething();
			return "done";
		}
		
		@RequestMapping("/future")
		public CompletableFuture<String> future() {
			return futureTestService.doSomething();
		}
		
	}
	
	@TestConfiguration
	public static class Config {
		
		@Bean
		public TestController testController() {
			return new TestController();
		}
	}
}
