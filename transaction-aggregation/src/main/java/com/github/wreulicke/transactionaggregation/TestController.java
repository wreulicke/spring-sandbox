package com.github.wreulicke.transactionaggregation;

import java.util.Objects;
import java.util.stream.IntStream;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	private final TestService testService;
	
	public TestController(TestService testService) {
		this.testService = testService;
	}
	
	@GetMapping("/test")
	@Transactional
	public Response test() {
		IntStream.range(0, 10)
			.mapToObj(Objects::toString)
			.map(TestService.Model::new)
			.forEach(testService::execute);
		
		return new Response();
	}
}
