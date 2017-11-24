package com.github.wreulicke.spring;

import org.springframework.stereotype.Component;

@Component("another")
public class AnotherService implements SampleService {
	
	@Override
	public String provide() {
		return "another";
	}
	
}
