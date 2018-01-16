package com.github.wreulicke.spring;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.MDC;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Execution;
import net.jodah.failsafe.RetryPolicy;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@RestController
@Slf4j
public class ComplexController {
	
	private MyService myService;
	
	
	ComplexController(ServerProperties serverProperties) {
		Integer port = serverProperties.getPort();
		log.info("port is {}", port);
		
		OkHttpClient client = new OkHttpClient.Builder()
			.addInterceptor(new HttpLoggingInterceptor().setLevel(Level.BASIC))
			.build();
		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl("http://localhost:" + port + "/")
			.client(client)
			.addCallAdapterFactory(RxJava2CallAdapterFactory
				.createAsync())
			.addConverterFactory(JacksonConverterFactory.create())
			.build();
		myService = retrofit.create(MyService.class);
	}
	
	
	private final CircuitBreaker circuitBreaker = new CircuitBreaker()
		.withFailureThreshold(2)
		.withSuccessThreshold(5)
		.withDelay(1, TimeUnit.SECONDS);
	
	
	@GetMapping("/backend/{number}")
	public Map<String, String> backend(@PathVariable("number") int number) {
		if (number < 5) {
			throw new RuntimeException(number + " is received");
		}
		if (number < 11) {
			return Collections.singletonMap("message", "success:" + number);
		}
		throw new RuntimeException(number + " is received");
	}
	
	@GetMapping("/frontend/{number}")
	public Single<Map<String, String>> frontend(@PathVariable("number") int number) {
		MDC.put("requestId", UUID.randomUUID().toString());
		RetryPolicy retryPolicy = new RetryPolicy()
			.withMaxRetries(2)
			.withDelay(100, TimeUnit.MILLISECONDS);
		Execution execution = new Execution(retryPolicy);
		
		if (circuitBreaker.allowsExecution() == false) {
			log.info("circuit is open. {}", number);
			return Single.error(new RuntimeException("circuit is open."));
		}
		log.info("starting... {}", number);
		return myService.call(number)
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.computation())
			.onErrorResumeNext(error -> ob -> {
				log.info("io failed.");
				ob.onError(new RuntimeException("io failed."));
			})
			.doOnSuccess(ignore -> {
				log.info("circuit breaker records success");
				circuitBreaker.recordSuccess();
			})
			.retryWhen(attempts -> {
				log.info("retrying... 1");
				return attempts.flatMap(failure -> {
					log.info("retrying... 2");
					if (execution.canRetryOn(failure)) {
						// io スレッドで 100ミリ秒後に実行
						return Single.timer(
								execution.getWaitTime().toMillis(),
								TimeUnit.MILLISECONDS)
							.toFlowable();
					} else {
						// リトライ出来なかったので、サーキットブレイカーに失敗を蓄積
						circuitBreaker.recordFailure(failure);
						return Flowable.error(failure);
					}
				});
			})
			.subscribeOn(Schedulers.computation());
//		return Single.create((SingleOnSubscribe<Map<String, String>>) ob -> {
//			myService.call(number)
//				.subscribeOn(Schedulers.computation())
//				.subscribe((map, e) -> {
//					if (e != null) {
//						log.error("io failed. {}", number);
//						ob.onError(new RuntimeException("io failed."));
//					} else {
//						log.error("io scceeded. {}", number);
//						ob.onSuccess(map);
//					}
//				});
//		}).retryWhen(attempts -> {
//			return attempts.flatMap(failure -> {
//				if (execution.canRetryOn(failure)) {
//					// io スレッドで 100ミリ秒後に実行
//					return Single.timer(
//							execution.getWaitTime().toMillis(),
//							TimeUnit.MILLISECONDS, Schedulers.io())
//						.toFlowable();
//				} else {
//					// リトライ出来なかったので、サーキットブレイカーに失敗を蓄積
//					circuitBreaker.recordFailure(failure);
//					return Flowable.error(failure);
//				}
//			});
//		})
//			// 最初の処理は io スレッドで
//			.subscribeOn(Schedulers.io())
//			// レスポンスを返すのは comutationのスレッドで
//			.observeOn(Schedulers.computation());
	
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler
	public Map<String, String> handler(Exception e) {
		return Collections.singletonMap("message", e.getMessage());
	}
}
