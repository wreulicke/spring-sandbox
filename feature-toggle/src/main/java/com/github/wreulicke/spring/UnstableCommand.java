package com.github.wreulicke.spring;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.github.wreulicke.spring.SampleController.User;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;

import rx.Observable;
import rx.schedulers.Schedulers;

@Slf4j
public class UnstableCommand extends HystrixObservableCommand<ResponseEntity<User>> {
	
	private final RestTemplate template;
	
	private static final Setter setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("unstable_command"))
		.andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withCircuitBreakerEnabled(true)
			.withCircuitBreakerRequestVolumeThreshold(5).withCircuitBreakerSleepWindowInMilliseconds(5000));
	
	
	public UnstableCommand(RestTemplate template) {
		super(setter);
		this.template = template;
	}
	
	@Override
	protected Observable<ResponseEntity<User>> construct() {
		log.info("start");
		Observable<ResponseEntity<User>> ob = Observable.<ResponseEntity<User>> create(observer -> {
			log.info("expect async");
			observer.onStart();
			ResponseEntity<User> user = template.getForEntity("/user", User.class);
			observer.onNext(user);
			observer.onCompleted();
		}).subscribeOn(Schedulers.io());
		log.info("end");
		return ob;
	}
	
	@Override
	protected Observable<ResponseEntity<User>> resumeWithFallback() {
		return Observable.just(ResponseEntity.status(503).build());
	}
	
}
