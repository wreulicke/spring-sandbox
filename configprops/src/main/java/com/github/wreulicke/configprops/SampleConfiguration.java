package com.github.wreulicke.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleConfiguration {
	
	@Bean
	@ConfigurationProperties("config.hoge")
	SampleConfig configHoge() {
		return new SampleConfig();
	}
	
	@Bean
	@ConfigurationProperties("config.fuga")
	SampleConfig configFuga() {
		return new SampleConfig();
	}
}
