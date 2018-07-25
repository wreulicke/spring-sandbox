

こんにちは。事業開発部の齋藤です。
今回はSpring BootのOSSのライブラリを紹介します。

使ってみたライブラリは、ZalandoというEC系の会社が出している [Tracer](https://github.com/zalando/tracer)というJavaのライブラリです。

今回のライブラリとSlf4jとその実装である Logback を組み合わせることで
サーバサイドのエラーが、どのAPIリクエストでエラーが発生したのか調べられるかもしれません。

やっていきましょう。

## はじめに

slf4jをお使いの皆さんはご存知な気がしますが
MDCというものをご存知でしょうか？

MDCとは Mapped Diagnostic Contextの略でSlf4jにAPIが存在します。
Slf4j APIの実装である、logbackの日本語のドキュメントでは
診断コンテキストと表現されています。

logbackでは、ログにMDCに入っている値を含めることが設定可能です。
さて、MDCとはどんなことに使えるのでしょうか？ということで
logbackのドキュメントを紹介しておきますので
そちらをご参照ください。

* [第8章 診断コンテキスト](https://logback.qos.ch/manual/mdc_ja.html)
* [英語ドキュメント](https://logback.qos.ch/manual/mdc.html)

今回紹介するライブラリは、Spring Bootにおいては、以下の手順で利用可能です。

* 依存関係を追加する
* 設定を追加する

ね、簡単でしょ？

## Tracerとは

今回は `Call tracing and log correlation in distributed systems` とあります。

このライブラリは以下のサポートを持っており、非常に簡単に始められます。

* サーブレットコンテナやApache Http Client、OkHttp、Hystrix、JUnit、AspectJなどのサポート
* Spring Boot向けのAuto Configuration

また、MDCのインテグレーションを有効にすることで
リクエストからレスポンスまでに発生したログを調べることが可能です。

Java8以降のサポートということなのでご注意ください。

## ライブラリを追加する

普段通り、spring bootのアプリケーションを作成します。

作成したspring bootのアプリケーションに以下の依存関係を追記します。

```groovy
dependencies {
  // なんかこの辺に
  // いっぱい
  // ライブラリが
  // 並んでるはず

  // 追加した
  compile "org.zalando:tracer-spring-boot-starter:0.17.0"
}
```

というわけで、ライブラリの追加は完了です。

mavenの方は以下のような記述を追加してください。

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.zalando</groupId>
      <artifactId>tracer-bom</artifactId>
      <version>${tracer.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
      <groupId>org.zalando</groupId>
      <artifactId>tracer-spring-boot-starter</artifactId>
  </dependency>
<dependencies>
```

## 設定を追加する

application.propertiesに以下の記述を追加しました。

```
tracer.traces.traceID= uuid
```

Tracer自身の設定の追加は以上です。

これで利用可能です。

ログにこのtraceIDを出したいので
logbackの設定も弄っておきます。

```xml
<?xml version="1.0"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <Target>System.out</Target>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy/MM/dd HH:mm:ss.SSS,JST}+0900 [%.16thread] %-5level %logger{36}:%line traceID:%X{traceID} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>

```

## 実際にどうなるのか検証してみる

これだけだと、どうなるんじゃいというわけですが
いくつかのサンプルコードと設定で紹介します。

なんの変哲もない、Spring Bootのメインクラスと
なんの変哲もない、Spring Web MVCのコントローラを用意します。

コントローラ内でログを吐きます。

```java
@EnableScheduling // 後で使う
@SpringBootApplication
public class MyApplication {
  public static void main(String... args) {
    SpringApplication.run(MyApplication.class, args);
  }
}

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.zalando.tracer.Tracer;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MyController {
	
	private final Tracer tracer;
	
	@GetMapping("/test")
	public String test() {
		log.info("Hello World!!");
		return "Hello World";
	}
}
```

テストコードで上で作ったコントローラにアクセスします。

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class TestEndpointTest {
	
	private static final Logger log = LoggerFactory.getLogger(TestEndpointTest.class);
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Test
	public void test() {
    ResponseEntity<String> response = testRestTemplate.getForEntity("/test", String.class);
    // レスポンスヘッダも入ってるはず
		log.info("traceID:{}", response.getHeaders().get("traceID"));
	}
}
```

テストを実行してみると・・・

以下のようなログが出力されました！
レスポンスヘッダにもちゃんと入ってそう！

```
2018/07/25 09:28:50.501+0900 [io-auto-1-exec-1] INFO  c.g.wreulicke.tracer.MyController:20 traceID:4b285cfd-6c93-48e7-ad6a-526ba57c013c - Hello World!!
2018/07/25 09:28:50.551+0900 [main] INFO  c.g.w.tracer.TestEndpointTest:27 traceID: - traceID:[4b285cfd-6c93-48e7-ad6a-526ba57c013c]
```

というわけで、リクエストをある程度時間で区切れば、トレース用のIDを使うことで、リクエスト単位のログを出力することが可能です。
また、クライアント側でもヘッダから値をログ出力しておくことで、サーバー側のログと突合することが可能です。
サーバから、予期しないレスポンスが帰ってきた際に簡単に調べることが可能になります。
マイクロサービスが流行っている中、こういったツールでも リクエストからレスポンスまでのトレースが可能ですね。

最近だとOpen TracingといったAPMなどのツールもあると思いますので
そちらも検討したいところですが
こういったアプローチも検討してはいかがでしょうか？

## 次は @Scheduling と一緒に

TracerのSpring Bootのintegrationには、`@Scheduling` という Springのアノテーションに対して
AspectJでTracerのHookを仕掛けてくれます。

なので以下のようなコードを用意して
Spring Bootのアプリケーションを起動してみます。

```java
@Slf4j
@Component
public class SchedlingTask {
	
	@Scheduled(fixedDelay = 1000)
	public void task(){
		log.info("done.");
	}
}
```

アプリケーションを起動してみました。今回はgradleでspring boot 1系なので
`./gradlew bootRun` です。

タスク1回に紐づくIDが割り振りされています！
これはすごいのでは！

```
2018/07/25 09:40:23.906+0900 [pool-2-thread-4] INFO  c.g.wreulicke.tracer.SchedlingTask:14 traceID:43df504d-1e3e-446b-bc6a-f1fe98c0b45d - done.
2018/07/25 09:40:24.909+0900 [pool-2-thread-4] INFO  c.g.wreulicke.tracer.SchedlingTask:14 traceID:0d10f76d-dc0d-482f-9c36-1acc3fd8f5dc - done.
2018/07/25 09:40:25.911+0900 [pool-2-thread-4] INFO  c.g.wreulicke.tracer.SchedlingTask:14 traceID:bd0d3b44-4047-4cff-abb4-a663fa2658bd - done.
2018/07/25 09:40:26.915+0900 [pool-2-thread-4] INFO  c.g.wreulicke.tracer.SchedlingTask:14 traceID:473f6114-9577-4619-ab0f-c28a36ebe133 - done.
2018/07/25 09:40:27.920+0900 [pool-2-thread-4] INFO  c.g.wreulicke.tracer.SchedlingTask:14 traceID:dd64abf9-ab7a-411c-a66f-e4d9517e789d - done.
```

バックグラウンドで動く処理はトラッキングが複雑になりがちで
ログから追いかけることは難しいことがあります。

そのため、こういったIDで検索することができれば
簡単に1回のタスクの流れを追いかけることが出来ますね！

## 応用編

さて、ここまではライブラリの紹介でしたが
今自分達のチームで行っている話として ログをJSONで出力したりしています。

logback-json-classic という、ライブラリを追加して
logbackで、以下のような設定を使っています。（本物はもうちょっとゴチャゴチャしています。）

```xml
<?xml version="1.0"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <Target>System.out</Target>
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="ch.qos.logback.contrib.json.classic.JsonLayout">
                <jsonFormatter class="ch.qos.logback.contrib.jackson.JacksonJsonFormatter">
                </jsonFormatter>
                <timestampFormat>yyyy-MM-dd'T'HH:mm:ss.SSSXXX</timestampFormat>
                <includeContextName>false</includeContextName>
                <appendLineSeparator>true</appendLineSeparator>
            </layout>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

この設定で 再度アプリケーションを起動してみます。

以下のようなログが出力されます！

```
{"timestamp":"2018-07-25T09:49:58.616+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"1ba95116-5095-49ca-a5ef-cf2464f1c8cf"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
{"timestamp":"2018-07-25T09:49:59.619+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"b7826e88-7a93-4654-95a8-78ebc221d511"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
{"timestamp":"2018-07-25T09:50:00.621+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"8078fb96-6ae2-4053-a7e4-45c16236982f"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
{"timestamp":"2018-07-25T09:50:01.626+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"aee72a6c-cc20-48bc-b17d-2bc7879d0abb"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
{"timestamp":"2018-07-25T09:50:02.630+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"2b42ce3a-cb94-48b2-b020-ebf299cb14bc"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
{"timestamp":"2018-07-25T09:50:03.634+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"e73eab66-cbc0-4a12-92d8-ee51f98040fa"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
{"timestamp":"2018-07-25T09:50:04.637+09:00","level":"INFO","thread":"pool-2-thread-3","mdc":{"traceID":"dbc64d80-163a-47b8-8b6b-da42b92c2dba"},"logger":"com.github.wreulicke.tracer.SchedlingTask","message":"done."}
```

この設定で吐いたログをCloudWatch Logsに送ると
以下のようなクエリでログを調べることが可能になります！

```
{ $.mdc.traceID = "dbc64d80-163a-47b8-8b6b-da42b92c2dba" }
```

詳しいドキュメントは こちら ([フィルタとパターンの構文](https://docs.aws.amazon.com/ja_jp/AmazonCloudWatch/latest/logs/FilterAndPatternSyntax.html)) をどうぞ！

## まとめ

今回は Zalando社がOSSとして出している、[Tracer](https://github.com/zalando/tracer) を紹介しました。
このライブラリを使うことで、ログが追いかけやすくなることが分かったかと思います。

しかし、このライブラリの本領はもっと他のライブラリと組み合わせた時に発揮されるように思われます。

Netflix OSSとして有名な[Hystrix](https://github.com/Netflix/Hystrix)のインテグレーションも用意されており
試しておきたいところです。

記事の本編についてはここでオシマイです。

## 余談: ライブラリを作っている会社について

また、このライブラリを作っているZalando社は
OSSとして色々なものを作っており、Javaで言うと、以下のようなライブラリを提供しています。

* アクセスログのためのライブラリ [logbook](https://github.com/zalando/logbook)
* 自由なレスポンスマッピングが可能なHttpClient [riptide](https://github.com/zalando/riptide)

またOSSへの取り組み方のドキュメントやRestful APIのガイドラインなど
自社の考えをオープンに公開しています。

* [Zalando社のOSSへの取り組み方に関するドキュメント](https://opensource.zalando.com/docs/releasing/index/)
* [Developing Restful APIs: A Comprehensive Set of Guidelines by Zalando](https://github.com/zalando/restful-api-guidelines)
  こちらは、[@kawasima](https://twitter.com/kawasima) さんが和訳したものがあります [Zalando RESTful API と イベントスキーマのガイドライン](https://restful-api-guidelines-ja.netlify.com/) 。 


また、Restful API Guidelineをうまく活用するためなのか
以下のようなツールまで作成しています。
Intellijでswaggerを書いていて
zallyというツールを使ってCIでAPI定義のLintまでやっているように見えます・・・。すごいですね。

* [intellij-swagger](https://github.com/zalando/intellij-swagger)
* [zally - A minimalistic, simple-to-use API linter ](https://github.com/zalando/zally)

zalando社では色々なライブラリを公開していく上で
以下のorganizationに分かれているようです。
色々なOSSが公開されていて興味深いです。

* [zalando](https://github.com/zalando)
* [zalando-incubator](https://github.com/zalando-incubator)
* [zalandoresearch](https://github.com/zalandoresearch)

僕が個人的に触ってみたいなーと思うのが
以下のOSSです。

* [authmosphere - A library to support OAuth2 workflows in JavaScript projects](https://github.com/zalando-incubator/authmosphere)

サンプルコードを見ると、express向けに、OAuth2のミドルウェアを提供しているようです。
便利そうですね。

と、なんか筆が載ってしまい
Zalando社の紹介になってしまった感じがありますが
記事はこれで本当にお終いです。

ここまで読んでいただきありがとうございました。