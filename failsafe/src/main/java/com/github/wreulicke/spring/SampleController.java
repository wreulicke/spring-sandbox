package com.github.wreulicke.spring;

import java.util.concurrent.atomic.LongAdder;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SampleController {
	
	private final LongAdder longAdder = new LongAdder();
	
	
	@GetMapping("/user/{username}")
	public User get(@PathVariable("username") String username) {
		return new User(username);
	}
	
	@GetMapping("/user")
	public User get() {
		long number = longAdder.longValue();
		log.info("received {}", number);
		longAdder.increment();
		if (number < 5) {
			throw new RuntimeException(number + " is received");
		}
		if (number < 11) {
			return new User("number: " + number);
		}
		throw new RuntimeException(number + " is received");
	}
	
	
	@Value
	public static class User {
		private final String username;
	}
}
