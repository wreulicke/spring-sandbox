こんにちは。齋藤です。

今回は 簡易的な方法で Spring Bootで APIのレスポンスまでにかかった時間を記録する方法を試してみます。

## 方法

WebRequestInterceptorを登録してWebMvcの設定を追加するだけです。

以下のようなコードを用意します。

preHandleでrequestが始まった時間を記録させておくことで
afterCompletionでレスポンスまでにかかった時間を記録することが可能です。

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
```

上記実装で空の実装になっている部分に関してはドキュメントを参照してください。

* [WebRequestInterceptor](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/context/request/WebRequestInterceptor.html)
* [AsyncWebRequestInterceptor](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/context/request/AsyncWebRequestInterceptor.html)

また、上記の WebRequestInterceptor を WebMvcConfigurerAdaptorのメソッドを経由して登録する必要があります。
今回は以下のようなコードを用意しました。

``` java
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addWebRequestInterceptor(new SampleInterceptor());
	};
}
```

テストコードでAPIにアクセスしてみたところ以下のようなログが出力されました。

```java
2018/01/16 16:17:32.458+0900 0a91fcd1-f99b-4b3b-b637-51448b1abde2 [http-nio-8080-exec-9] INFO  com.github.wreulicke.spring.SampleInterceptor:42 - URI /rxjava, status code: 500, response time: 2000000 (ns)
2018/01/16 16:17:32.463+0900 0bb216bb-c055-4aa6-a494-f59a88ac927a [http-nio-8080-exec-10] INFO  com.github.wreulicke.spring.MyController:42 - test
2018/01/16 16:17:32.464+0900 0bb216bb-c055-4aa6-a494-f59a88ac927a [http-nio-8080-exec-10] INFO  com.github.wreulicke.spring.MyController:49 - circuit is open.
2018/01/16 16:17:32.467+0900 0bb216bb-c055-4aa6-a494-f59a88ac927a [http-nio-8080-exec-10] INFO  com.github.wreulicke.spring.SampleInterceptor:42 - URI /rxjava, status code: 500, response time: 1000000 (ns)
```

また今回の例では、APIのコードは紹介していませんでしたが
今回のサンプルでは、サーブレットの非同期処理を使ったAPIでテストをしています。

## まとめ

実際に利用したコードは以下のリポジトリに配置してあります。

[サンプルコードはこちら](https://github.com/wreulicke/spring-sandbox/tree/7422de530b0509041455450b1eddceedfd3b37c5/failsafe)

## 参考

* [SpringFrameworkのHandlerInterceptorで共通処理](http://chronosdeveloper.hatenablog.com/entry/2014/12/15/013731)