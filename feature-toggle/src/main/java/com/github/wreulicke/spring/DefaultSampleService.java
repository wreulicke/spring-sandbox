package com.github.wreulicke.spring;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class DefaultSampleService implements SampleService {
	
	@Override
	public String provide() {
		return "default";
	}
	
}
