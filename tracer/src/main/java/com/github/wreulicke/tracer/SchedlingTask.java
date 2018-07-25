package com.github.wreulicke.tracer;

import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SchedlingTask {
	
	@Scheduled(fixedDelay = 1000)
	public void task() {
		log.info("done.");
	}
}
