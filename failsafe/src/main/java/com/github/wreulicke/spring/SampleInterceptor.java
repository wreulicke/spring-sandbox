package com.github.wreulicke.spring;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.AsyncWebRequestInterceptor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

@Slf4j
public class SampleInterceptor implements AsyncWebRequestInterceptor {
	
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
			request.removeAttribute(SampleInterceptor.class.getName(), RequestAttributes.SCOPE_REQUEST);
			throw new IllegalStateException();
		}
		LocalDateTime dateTime = (LocalDateTime) object;
		if (request instanceof NativeWebRequest) {
			NativeWebRequest nativeWebRequest = (NativeWebRequest) request;
			HttpServletRequest servletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
			HttpServletResponse servletResponse = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
			log.info("URI {}, status code: {}, response time: {} (ns)",
					servletRequest.getRequestURI(),
					servletResponse.getStatus(),
					Duration.between(dateTime, LocalDateTime.now()).toNanos());
		}
		request.removeAttribute(SampleInterceptor.class.getName(), RequestAttributes.SCOPE_REQUEST);
	}
	
	@Override
	public void afterConcurrentHandlingStarted(WebRequest request) {
	}
	
}
