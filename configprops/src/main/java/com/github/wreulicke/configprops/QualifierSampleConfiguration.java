package com.github.wreulicke.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class QualifierSampleConfiguration {
	
	@Bean
	@Hoge
	@ConfigurationProperties("config.hoge")
	SampleConfig qualifierConfigHoge() {
		return new SampleConfig();
	}
	
	@Bean
	@Fuga
	@ConfigurationProperties("config.fuga")
	SampleConfig qualifierConfigFuga() {
		return new SampleConfig();
	}
	
	@Bean
	@ConfigurationProperties("config.default")
	@Primary
	SampleConfig config() {
		return new SampleConfig();
	}
	
}
