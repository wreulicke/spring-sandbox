package com.github.wreulicke.spring;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.MDC;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

@SpringBootApplication
@Slf4j
public class MyApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}
	
	
	@AllArgsConstructor
	public static class ContextObservable<T>extends Observable<T> {
		
		private final Map<String, String> map = MDC.getCopyOfContextMap();
		
		private final Observable<T> observable;
		
		
		@Override
		protected void subscribeActual(Observer<? super T> observer) {
			Map<String, String> before = MDC.getCopyOfContextMap();
			MDC.setContextMap(map);
			
			try {
				observable.subscribe(observer);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (before == null) {
					MDC.clear();
				} else
					MDC.setContextMap(before);
			}
		}
	}
	
	@AllArgsConstructor
	public static class ContextualSingle<T>extends Single<T> {
		
		public final Map<String, String> map = MDC.getCopyOfContextMap();
		
		private final Single<T> single;
		
		
		@Override
		protected void subscribeActual(SingleObserver<? super T> observer) {
			Map<String, String> before = MDC.getCopyOfContextMap();
			
			if (map != null) {
				MDC.setContextMap(map);
			}
			
			try {
				single.subscribe(observer);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (before == null) {
					MDC.clear();
				} else
					MDC.setContextMap(before);
			}
		}
	}
	
	@RequiredArgsConstructor
	public static class ContextualObserver<T> implements SingleObserver<T> {
		
		private final Map<String, String> map;
		
		private final SingleObserver<T> ob;
		
		
		public static <T> SingleObserver<T> wrap(final Map<String, String> map,
				final SingleObserver<T> ob) {
			return new ContextualObserver<>(map, ob);
		}
		
		@Override
		public void onSubscribe(Disposable d) {
			Map<String, String> before = MDC.getCopyOfContextMap();
			
			if (map != null) {
				MDC.setContextMap(map);
			}
			
			try {
				ob.onSubscribe(d);
			} finally {
				if (before == null) {
					MDC.clear();
				} else {
					MDC.setContextMap(before);
				}
			}
		}
		
		@Override
		public void onSuccess(T t) {
			Map<String, String> before = MDC.getCopyOfContextMap();
			
			if (map != null) {
				MDC.setContextMap(map);
			}
			
			try {
				ob.onSuccess(t);
			} finally {
				if (before == null) {
					MDC.clear();
				} else
					MDC.setContextMap(before);
			}
			
		}
		
		@Override
		public void onError(Throwable e) {
			Map<String, String> before = MDC.getCopyOfContextMap();
			
			if (map != null) {
				MDC.setContextMap(map);
			}
			
			try {
				ob.onError(e);
			} finally {
				if (before == null) {
					MDC.clear();
				} else
					MDC.setContextMap(before);
			}
			
		}
	}
}
