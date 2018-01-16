
こんにちは。齋藤です。

今回は 簡易的な方法で Spring Bootで APIのスループットを記録する方法を試してみます。

## 方法

WebRequestInterceptorを登録してWebMvcの設定を追加するだけです。

以下のようなコードを用意します。

preHandleでrequestが始まった時間を記録させておくことで
afterCompletionで簡易的なスループットの計算が可能です。

``` java

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
			log.info("URI {}, status code: {}, throughput: {} (ns)",
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
```

上記実装で空の実装になっている部分に関してはドキュメントを参照してください。

* https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/context/request/WebRequestInterceptor.html
* https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/context/request/AsyncWebRequestInterceptor.html

また、上記の WebRequestInterceptor を WebMvcConfigurerAdaptorのメソッドを経由して登録する必要があります。
今回は以下のようなコードを用意しました。

``` java
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addWebRequestInterceptor(new SampleInterceptor());
	};
}
```

## まとめ

必要になったので書きました。

実際に利用したコードは以下のリポジトリに配置してあります。

https://github.com/wreulicke/spring-sandbox/tree/a6b51ba352918e0fad05a796cf7aefe1e824c34b/failsafe

## 参考

* http://chronosdeveloper.hatenablog.com/entry/2014/12/15/013731