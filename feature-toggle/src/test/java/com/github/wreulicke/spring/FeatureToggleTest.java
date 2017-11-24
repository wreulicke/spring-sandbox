package com.github.wreulicke.spring;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.ff4j.FF4j;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FeatureToggleTest {
	
	@Autowired
	FF4j ff4j;
	
	@Autowired
	SampleService sampleService;
	
	
	@Test
	public void test() {
		assertThat(sampleService.provide()).isEqualTo("default");
		ff4j.enable("sample-feature");
		assertThat(sampleService.provide()).isEqualTo("another");
	}
}
