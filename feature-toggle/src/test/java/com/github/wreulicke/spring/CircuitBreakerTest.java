package com.github.wreulicke.spring;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CircuitBreakerTest {
	
	ExecutorService service = Executors.newCachedThreadPool();
	
	@Autowired
	TestRestTemplate template;
	
	@Autowired
	RestTemplate spy;
	
	
	@Before
	public void setup() {
	}
	
	@Test
	public void testX() throws Exception {
		Stream<Future<ResponseEntity<String>>> stream = IntStream.range(0, 1000)
			.mapToObj(ignore -> service.submit(() -> template.getForEntity("/user/_unstable", String.class)));
		stream.forEach(f -> {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
			}
		});
		template.getForEntity("/user/_unstable", String.class);
		template.getForEntity("/user/_unstable", String.class);
		template.getForEntity("/user/_unstable", String.class);
		template.getForEntity("/user/_unstable", String.class);
	}
	
	@Test
	public void testV() throws Exception {
		Stream<Future<ResponseEntity<String>>> stream = IntStream.range(0, 1000)
			.mapToObj(ignore -> service.submit(() -> template.getForEntity("/user/_test", String.class)));
		stream.forEach(f -> {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
			}
		});
		template.getForEntity("/user/_test", String.class);
		template.getForEntity("/user/_test", String.class);
		template.getForEntity("/user/_test", String.class);
		template.getForEntity("/user/_test", String.class);
	}
	
	@Test
	public void testZ() throws Exception {
		Stream<Future<ResponseEntity<String>>> stream = IntStream.range(0, 1000)
			.mapToObj(ignore -> service.submit(() -> template.getForEntity("/user/_unstable_command", String.class)));
		stream.forEach(f -> {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
			}
		});
		template.getForEntity("/user/_unstable_command", String.class);
		template.getForEntity("/user/_unstable_command", String.class);
		template.getForEntity("/user/_unstable_command", String.class);
		template.getForEntity("/user/_unstable_command", String.class);
	}
	
	
	@TestConfiguration
	public static class Config {
		
		@Bean
		RestTemplate template(TestRestTemplate testRestTemplate) {
			return testRestTemplate.getRestTemplate();
		}
	}
}
