package com.github.wreulicke.spring;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Slf4j
public class SampleInterceptor extends HandlerInterceptorAdapter {
	
	private static String HANDLER_NAME = SampleInterceptor.class.getName() + ".handler";
	
	private static String PRE_HANDLE_TIME = SampleInterceptor.class.getName() + ".preHandleTime";
	
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		if (request.getAttribute(PRE_HANDLE_TIME) == null) {
			request.setAttribute(PRE_HANDLE_TIME, LocalDateTime.now());
		}
		if (request.getAttribute(HANDLER_NAME) == null) {
			Object handlerPath = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
			if (handlerPath == null) {
				NativeWebRequest nativeWebRequest = (NativeWebRequest) request;
				HttpServletRequest servletRequest = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
				handlerPath = servletRequest.getServletPath();
			}
			request.setAttribute(HANDLER_NAME, handlerPath);
		}
		
		return true;
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		Object object = request.getAttribute(PRE_HANDLE_TIME);
		try {
			if (object instanceof LocalDateTime == false) {
				throw new IllegalStateException();
			}
			LocalDateTime dateTime = (LocalDateTime) object;
			log.info("URI {}, status code: {}, response time: {} (ms)",
					request.getAttribute(HANDLER_NAME),
					response.getStatus(),
					Duration.between(dateTime, LocalDateTime.now()).toMillis());
		} finally {
			request.removeAttribute(PRE_HANDLE_TIME);
			request.removeAttribute(HANDLER_NAME);
		}
	}
	
}
