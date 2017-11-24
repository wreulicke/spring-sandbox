package com.github.wreulicke.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.ff4j.FF4j;

@Configuration
public class SampleConfiguration {
	
	@Bean
	public static FF4j ff4j() {
		return new FF4j().autoCreate(true);
	}
}
