package com.github.wreulicke.spring;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

@Component
@Slf4j
public class SampleInterceptor implements WebRequestInterceptor {
	
	@Override
	public void preHandle(WebRequest request) throws Exception {
		request.setAttribute(SampleInterceptor.class.getName(), LocalDateTime.now(), RequestAttributes.SCOPE_REQUEST);
	}
	
	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
	}
	
	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
		Object object = request.getAttribute(SampleInterceptor.class.getName(), RequestAttributes.SCOPE_REQUEST);
		if (object instanceof LocalDateTime == false) {
			throw new IllegalStateException();
		}
		LocalDateTime dateTime = (LocalDateTime) object;
		log.info("throughput {}", Duration.between(dateTime, LocalDateTime.now()).toNanos());
		request.removeAttribute(SampleInterceptor.class.getName(), RequestAttributes.SCOPE_REQUEST);
	}
	
}
