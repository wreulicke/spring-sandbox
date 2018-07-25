package com.github.wreulicke.tracer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.tracer.Tracer;
import org.zalando.tracer.httpclient.TracerHttpRequestInterceptor;
import org.zalando.tracer.unit.TracerRule;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class TestEndpointTest {
	
	private static final Logger log = LoggerFactory.getLogger(TestEndpointTest.class);
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	RestTemplateBuilder restTemplateBuilder;
	
	@Autowired
	@Rule
	public TracerRule tracerRule;
	
	@LocalServerPort
	int port;
	
	@Test
	public void test() {
		ResponseEntity<String> response = testRestTemplate.getForEntity("/test", String.class);
		restTemplateBuilder.build().getForEntity("http://localhost:" + port + "/test", String.class);
		log.info("X-Trace-ID:{}", response.getHeaders().get("X-Trace-ID"));
	}
	
	@TestConfiguration
	public static class TestRestTemplateCustomizer {
		
		@Bean
		public TracerRule tracerRule(Tracer tracer) {
			return new TracerRule(tracer);
		}
		
		@Bean
		public RestTemplateBuilder restTemplateBuilder(Tracer tracer, Logbook logbook) {
			CloseableHttpClient httpClient = HttpClientBuilder.create()
				.addInterceptorFirst(new TracerHttpRequestInterceptor(tracer))
				.addInterceptorLast(new LogbookHttpRequestInterceptor(logbook))
				.addInterceptorLast(new LogbookHttpResponseInterceptor()).build();
			HttpComponentsClientHttpRequestFactory factory =
				new HttpComponentsClientHttpRequestFactory(httpClient);
			return new RestTemplateBuilder().requestFactory(factory);
		}
		
	}
}
