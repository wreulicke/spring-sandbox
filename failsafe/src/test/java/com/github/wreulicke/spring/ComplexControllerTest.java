package com.github.wreulicke.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.wreulicke.spring.MyApplication.ContextObservable;
import com.github.wreulicke.spring.MyApplication.ContextualObserver;
import com.github.wreulicke.spring.MyApplication.ContextualSingle;

import io.reactivex.plugins.RxJavaPlugins;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
	"security.basic.enable=false",
	"security.ignored=/**",
	"server.connection-timeout=-1",
	"spring.mvc.async.request-timeout=-1"
}, webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8080")
@Slf4j
public class ComplexControllerTest {
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	
	@BeforeClass
	public static void setup() {
		RxJavaPlugins.setOnSingleAssembly(single -> {
			return new ContextualSingle<>(single);
		});
		RxJavaPlugins.setOnSingleSubscribe((single, ob) -> {
			if (single instanceof ContextualSingle) {
				return new ContextualObserver(((ContextualSingle) single).map, ob);
			}
			return ob;
		});
		RxJavaPlugins.setOnObservableAssembly(ob -> {
			return new ContextObservable<>(ob);
		});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void rxjava() throws InterruptedException {
		// サーキットブレイカーはこのタイミングだと閉じている
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 1))
			.contains(entry("message", "io failed."));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 2))
			.contains(entry("message", "io failed."));
		// 2回失敗したので開いている
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 3))
			.contains(entry("message", "circuit is open."));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 4))
			.contains(entry("message", "circuit is open."));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 5))
			.contains(entry("message", "circuit is open."));
		// 2秒待つと半開に移る
		Thread.sleep(2000);
		// 次は5回成功する
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 6))
			.contains(entry("message", "success:6"));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 7))
			.contains(entry("message", "success:7"));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 8))
			.contains(entry("message", "success:8"));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 9))
			.contains(entry("message", "success:9"));
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 10))
			.contains(entry("message", "success:10"));
		// 次は失敗するが、上記5回の成功によってサーキットブレイカーが復帰し（閉じ）ている
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 11))
			.contains(entry("message", "io failed."));
		// 上のタイミングで、サーキットブレイカーが半開であったなら、上記呼び出しによって
		// サーキットブレイカーが閉じており、次は "circuit is open."を記録するはずであるが
		// この状態ではまだ閉じているので、その結果にはならない。
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 12))
			.contains(entry("message", "io failed."));
		// 2回失敗したのでまたサーキットブレイカーが閉じる
		assertThat(testRestTemplate.getForObject("/frontend/{number}", Map.class, 13))
			.contains(entry("message", "circuit is open."));
	}
}
