package com.github.wreulicke.configprops;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConfigpropsApplicationTests {
	
	@Autowired
	@Qualifier("configHoge")
	SampleConfig configHoge;
	
	@Autowired
	@Qualifier("configFuga")
	SampleConfig configFuga;
	
	@Autowired
	SampleConfig defaultConfig;
	
	@Autowired
	@Fuga
	SampleConfig qualifierConfigHoge;
	
	@Autowired
	@Hoge
	SampleConfig qualifierConfigFuga;
	
	
	@Test
	public void test1() {
		SampleConfig expected = new SampleConfig();
		expected.setName("hoge");
		expected.setTest("test1");
		assertThat(configHoge).isEqualTo(expected);
	}
	
	@Test
	public void test2() {
		SampleConfig expected = new SampleConfig();
		expected.setName("fuga");
		expected.setTest("test2");
		assertThat(configFuga).isEqualTo(expected);
	}
	
}
