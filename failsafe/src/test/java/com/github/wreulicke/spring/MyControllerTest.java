package com.github.wreulicke.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
	"security.basic.enable=false",
	"security.ignored=/**",
	"server.connection-timeout=-1",
	"spring.mvc.async.request-timeout=-1"
}, webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
public class MyControllerTest {
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void rxjava() throws InterruptedException {
		// サーキットブレイカーはこのタイミングだと閉じている
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "io failed."));
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "io failed."));
		// 2回失敗したので開いている
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "circuit is open."));
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "circuit is open."));
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "circuit is open."));
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "circuit is open."));
		// 2秒待つと半開に移る
		Thread.sleep(2000);
		// 半開になったので処理が実行されるがまたもや失敗。
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "io failed."));
		// 半開時に失敗したので サーキットブレイカーは開く
		assertThat(testRestTemplate.getForObject("/rxjava", Map.class))
			.contains(entry("message", "circuit is open."));
	}
}
