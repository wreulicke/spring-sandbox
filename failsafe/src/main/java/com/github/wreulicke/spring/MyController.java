package com.github.wreulicke.spring;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.MDC;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Execution;
import net.jodah.failsafe.RetryPolicy;

@RestController
@Slf4j
public class MyController {
	
	// 2回失敗で開く
	// 半開時に5回成功するとサーキットブレイカーが閉じる
	// 開いたあとに半開に移るのが 1秒後
	private final CircuitBreaker circuitBreaker = new CircuitBreaker()
		.withFailureThreshold(2)
		.withSuccessThreshold(5)
		.withDelay(1, TimeUnit.SECONDS);
	
	
	@GetMapping("/rxjava")
	public Single<String> rxjava() {
		MDC.put("requestId", UUID.randomUUID().toString());
		log.info("test");
		RetryPolicy retryPolicy = new RetryPolicy()
			.withMaxRetries(2)
			.withDelay(100, TimeUnit.MILLISECONDS);
		Execution execution = new Execution(retryPolicy);
		
		if (circuitBreaker.allowsExecution() == false) {
			log.info("circuit is open.");
			return Single.error(new RuntimeException("circuit is open."));
		}
		log.info("starting...");
		return Single.create((SingleOnSubscribe<String>) ob -> {
			log.error("error occured.");
			ob.onError(new RuntimeException("io failed."));
			// 本来は成功時にサーキットブレイカーに成功を記録させる。
			// ob.onSuccess("test");
			// circuitBreaker.recordSuccess();
		}).retryWhen(attempts -> {
			return attempts.flatMap(failure -> {
				if (execution.canRetryOn(failure)) {
					// io スレッドで 100ミリ秒後に実行
					return Single.timer(
							execution.getWaitTime().toMillis(),
							TimeUnit.MILLISECONDS, Schedulers.io())
						.toFlowable();
				} else {
					// リトライ出来なかったので、サーキットブレイカーに失敗を蓄積
					circuitBreaker.recordFailure(failure);
					return Flowable.error(failure);
				}
			});
		})
			// 最初の処理は io スレッドで
			.subscribeOn(Schedulers.io())
			// レスポンスを返すのは comutationのスレッドで
			.observeOn(Schedulers.computation());
	}
	
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler
	public Map<String, String> handler(Exception e) {
		return Collections.singletonMap("message", e.getMessage());
	}
}
