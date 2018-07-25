package com.github.wreulicke.tracer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.zalando.tracer.Tracer;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MyController {
	
	private final Tracer tracer;
	
	@GetMapping("/test")
	public String test() {
		log.info("Hello World!!");
		return "Hello World";
	}
	
}
