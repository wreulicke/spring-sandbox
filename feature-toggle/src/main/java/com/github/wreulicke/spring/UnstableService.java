package com.github.wreulicke.spring;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.github.wreulicke.spring.SampleController.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
@Slf4j
public class UnstableService {
	
	@Autowired
	RestTemplate template;
	
	
	@HystrixCommand(commandKey = "test", fallbackMethod = "fallbackGetUser", commandProperties = {
		@HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
		@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
		@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000")
	})
	public ResponseEntity<User> unstableGetUser() {
		log.info("expect async");
		return template.getForEntity("/user", User.class);
	}
	
	public ResponseEntity<User> fallbackGetUser() {
		return ResponseEntity.status(503).build();
	}
}
