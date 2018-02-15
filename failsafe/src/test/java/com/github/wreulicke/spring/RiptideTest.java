package com.github.wreulicke.spring;

import static org.zalando.riptide.Bindings.on;
import static org.zalando.riptide.Navigators.series;
import static org.zalando.riptide.RoutingTree.dispatch;

import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.riptide.Bindings;
import org.zalando.riptide.Http;
import org.zalando.riptide.Navigators;
import org.zalando.riptide.RoutingTree;
import org.zalando.riptide.failsafe.FailsafePlugin;
import org.zalando.riptide.failsafe.RetryRoute;

import com.github.wreulicke.spring.SampleController.User;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.RetryPolicy;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
@Slf4j
public class RiptideTest {
	
	@LocalServerPort
	int port;
	
	
	@Test
	public void test() throws InterruptedException, ExecutionException {
		Http http = Http.builder()
			.requestFactory(new OkHttp3ClientHttpRequestFactory())
			.baseUrl("http://localhost:" + port)
			.converter(new StringHttpMessageConverter())
			.converter(new MappingJackson2HttpMessageConverter())
			.build();
		
		RoutingTree<Series> tree = dispatch(series(),
				on(Series.SUCCESSFUL).call(User.class, user -> {
					log.info(user.toString());
				}),
				on(Series.CLIENT_ERROR).call(() -> {
					log.error("client error");
				}),
				on(Series.SERVER_ERROR).call(() -> {
					log.error("server error");
				}));
		
		http.get("/user/{username}", "John")
			.dispatch(tree)
			.get();
	}
	
	@Test
	public void testWithCircuitBreaker() throws InterruptedException, ExecutionException {
		CircuitBreaker circuitBreaker = new CircuitBreaker()
			.withFailureThreshold(5)
			.withSuccessThreshold(2)
			.withDelay(1, TimeUnit.SECONDS);
		
		Http http = Http.builder()
			.requestFactory(new OkHttp3ClientHttpRequestFactory())
			.baseUrl("http://localhost:" + port)
			.converter(new StringHttpMessageConverter())
			.converter(new MappingJackson2HttpMessageConverter())
			.plugin(new FailsafePlugin(Executors.newScheduledThreadPool(20))
				.withRetryPolicy(new RetryPolicy()
					.retryOn(SocketTimeoutException.class)
					.withDelay(25, TimeUnit.MILLISECONDS)
					.withMaxRetries(10))
				.withCircuitBreaker(circuitBreaker))
			.build();
		
		http.get("/user")
			.dispatch(dispatch(series(),
					on(Series.SUCCESSFUL).call(User.class, user -> {
						log.info(user.toString());
					}),
					Bindings.anySeries().dispatch(Navigators.status(),
							on(HttpStatus.SERVICE_UNAVAILABLE).call(RetryRoute.retry()),
							Bindings.anyStatus().call(res -> {
								log.error("error: {}", res.getStatusCode());
							}))))
			.get();
		
	}
}
