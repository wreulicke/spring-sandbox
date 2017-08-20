## 前書き

前回の記事から１ヶ月ほど立っております。
少し涼しくなって来たでしょうか。今ものすごくラーメンが食べたい齋藤です。

この記事ではSpring Bootのテストを対象として
[ContextCustomizer](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/context/ContextCustomizer.html)を使ってテストを楽に書けるようにしてみます。

## 前提

本記事では、[Retrofit2](http://square.github.io/retrofit/)を使ったテストを例にやっていきます。
使用したソースは[こちらのリポジトリ](https://github.com/Wreulicke/spring-sandbox/tree/e41b17b8a922cfef1b646d0728a39910263527bd)においています。

ランダムポートで立ち上げたSpring Bootのサーバに対して
[Retrofit2](http://square.github.io/retrofit/)を使いAPIのテストを書きます。

なお、本記事では[AssertJ](http://joel-costigliola.github.io/assertj/)を使っています。

今回用意したのは以下の3つです

* `@SpringBootApplication`をつけたクラス `SampleApplication`
* `@RestController`をつけたコントローラ `SampleController`
* コントローラが返す`User`クラス

それぞれソースを以下に示しています。

```java
@SpringBootApplication
public class SampleApplication {	
	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
```

```java
@RequestMapping("/user")
@RestController
public class SampleController {
	@GetMapping
	public User user() {
		return new User("test");
	}
}
```

```java
@Value // lombokのアノテーション
public class User {
    private final String name;
}
```

また、Retrofitで使うインターフェースはこちらです。
Retrofitではコントローラと似たような感じのものを作ります。

```java
public interface UserEndpoint {
  @GET("/user")
  public Call<User> user();
}
```

## まずは素朴にテストを書いてみる

下記に素朴な形でテストを書いてみました。
どうでしょうか？Springマスターのあなたなら簡単でしょうか？

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserEndpointTestFirst {

  @Autowired
  WebApplicationContext wac;

  @LocalServerPort
  int port;

  Retrofit retrofit;

  @Before
  public void setUp() {
    String contextPath = wac.getEnvironment()
      .getProperty("server.context-path", "");
    retrofit = new Retrofit.Builder().baseUrl("http://localhost:" + port + contextPath)
      .addConverterFactory(JacksonConverterFactory.create())
      .build();
  }

  @Test
  public void test() throws IOException {
    UserEndpoint endpoint = retrofit.create(UserEndpoint.class);
    User user = endpoint.get()
      .execute()
      .body();
    assertThat(user).returns("test", User::getName);
  }
}
```

こういったテストが何個も並ぶと地獄ですね。もう少し楽にしてみましょう。
また、WebApplicationContextが強力なAPIを持っているので
テストを見たときにギョッとしてしまいます。(変なことしてないよね・・・？みたいな)

## もうちょっとスッキリさせて見る。

下記の形でスッキリしました！！！！！！！！！！
どうでしょうか？先ほどよりかはスッキリしているように見えます。

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserEndpointTestSecond extends TestBase {

  @Test
  public void test() throws IOException {
    UserEndpoint endpoint = retrofit.create(UserEndpoint.class);
    User user = endpoint.get()
      .execute()
      .body();

    assertThat(user).returns("test", User::getName);
  }
}
```

上記コードでは親クラスが指定されています。
特に難しいことはやっていません。`@Before`の処理を親クラスに入れただけです。
こんな感じ。

```java
public class TestBase {
  @Autowired
  private WebApplicationContext wac;

  @LocalServerPort
  private int port;

  protected Retrofit retrofit;

  @Before
  public void setUp() {
    String contextPath = wac.getEnvironment()
      .getProperty("server.context-path", "");
    retrofit = new Retrofit.Builder().baseUrl("http://localhost:" + port + contextPath)
      .addConverterFactory(JacksonConverterFactory.create())
      .build();
  }
}
```

ここでも、やはりというかWebApplicationContextのAPIを呼び出したりしています。
もう少しどうにかならないものでしょうか？？
テストをしたいのにも関わらず、Springの層が見えすぎている気がします。

## 脱線

Spring Bootの組み込みサーバを使ったテストでは
`TestRestTemplate`を使うことができます。

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserEndpointTestOther extends TestBase {
	
	@Autowired
	private TestRestTemplate template;
	
	@Test
	public void test() throws IOException {
		User user = template.getForObject("/user", User.class);
		assertThat(user).returns("test", User::getName);
	}
}
```

このAPIを使うの個人的に嫌な部分があって
`"/user"`という定数が突然出てくるわけですが（いやまぁAPIのエンドポイントなんですけども）
これが複数散乱することになります。
複雑なAPIになればなるほどテストも増えて、この`"/user"`がバラまかれる形になります。

辛くない？？

ところで、`TestRestTemplate`はどこから来たのでしょうか？
今回の記事のミソは`TestRestTemplate`はどこから来たのか、がミソになります。

## 早速ネタバラシです

`ContextCustomizer`を実装した`SpringBootTestContextCustomizer`というクラスが
[spring-boot-testの中に存在](https://github.com/spring-projects/spring-boot/blob/138b96cf5f9536aaecdc8ebcc86cbe00a557b6ae/spring-boot-test/src/main/java/org/springframework/boot/test/context/SpringBootTestContextCustomizer.java)します。

このクラスがTestRestTemplateをSpringのDIコンテナの中に登録しているおかげで
テストクラスにおいて`@Autowired`を使ってTestRestTemplateのDIができるわけです。

## 実装の前にテストをこんな風にしたい、というのを見てみる

実装を書くその前にどんな風にテスト書きたいかなぁと考えて見たところ
下みたいな感じで書けたら嬉しいですね。

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserEndpointTest {

  @Autowired
  Retrofit retrofit;

  @Test
  public void test() throws IOException {
    UserEndpoint endpoint = retrofit.create(UserEndpoint.class);
    User user = endpoint.get()
      .execute()
      .body();

    assertThat(user).returns("test", User::getName);
  }
}
```

## ContextCustomizerを使ってテストを楽にしてみる

ContextCustomizerを使って上記の形でテストを書けるようにしましたが
少し長くなってしまったので、ここにザッとまとめておきます。

* 登録したいBeanのFactoryクラスを作成する。
* ContextCustomizerで登録したいBeanを上記で作成したFactoryクラスと共に登録する 
* ContextCustomizerを生成するFactoryクラスを作成する。
* ContextCustomizerを生成するFactoryクラスを設定ファイルに記述しておく

では実装してみます。

部分部分を抽出して見ていきます。
以下のコードの大部分は先ほど紹介した`SpringBootTestContextCustomizer`のコードです。
少し見やすさのために簡略化・省略しています。

`SampleContextCustomizer`でbeanをBeanFactoryと共に登録し
`TestRetrofitFactory`はSpringのEnviromentクラス等からポートなどを取り出して
Retrofitオブジェクトを構築しています。

```java
public class SampleContextCustomizer implements ContextCustomizer {

  @Override
  public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedContextConfiguration) {
    SpringBootTest annotation = AnnotatedElementUtils.getMergedAnnotation(mergedContextConfiguration.getTestClass(), SpringBootTest.class);
    if (annotation.webEnvironment()
      .isEmbedded()) {
      ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
      if (beanFactory instanceof BeanDefinitionRegistry) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context;
        registry.registerBeanDefinition(Retrofit.class.getName(), new RootBeanDefinition(TestRetrofitFactory.class));
      }
    }
  }
  
  public static class TestRetrofitFactory implements FactoryBean<Retrofit>, ApplicationContextAware {
    // ...省略
    @Override
    public Retrofit getObject() throws Exception {
      String port = this.env.getProperty("local.server.port", "8080");
      String contextPath = this.resolver.getProperty("context-path", "");
      return new Retrofit.Builder().baseUrl((isSsl ? "https" : "http") + "://localhost:" + port + contextPath)
        .addConverterFactory(JacksonConverterFactory.create())
        .build();
    }
    // ...省略
  }
  // ...省略
}
```

また、これと同時に設定ファイルを追加しました。

`src/test/resources/META-INF/spring.factories`に以下の設定を書いています。
この設定ファイルは`SpringFactoriesLoader`によって読み込まれます。([javadoc](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/io/support/SpringFactoriesLoader.html))

```
org.springframework.test.context.ContextCustomizerFactory=\
com.github.wreulicke.spring.TestContextCustomizerFactory
```

この設定に追加したFactoryクラスは以下のコードです。

```java
public class SampleContextCustomizerFactory implements ContextCustomizerFactory {

  @Override
  public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
    if (AnnotatedElementUtils.findMergedAnnotation(testClass, SpringBootTest.class) != null) {
      return new SampleContextCustomizer();
    }
    return null;
  }

}
```

どうでしょうか。
この実装を追加することで簡単（？）にテストを書くことができるようになりました！！
これで先ほど見せたこんなテストが書きたいなー！というのが動くようになります。

今回のケースでは説明しませんでしたが
ライブラリとしてContextCustomizer等を切り出しておくことで
非常に簡単にRetrofitなど自分が使いたいクラスを使うことが可能になるでしょう。

また、今回ではFactoryの中にベタっと実装を書いたわけですが
ContextCustomizerFactoryのcreateContextCustomizerの引数からテストクラスが取得できるので
テストクラスにアノテーション等を使って外から設定を注入することができそうですね。

## まとめ

いかがだったでしょうか。

今回の記事では`ContextCustomizer`を使ってテストを簡単に書けるようにしてみました。
ライブラリにしておくと簡単に使い回せそうですね！

非常に強力な機能ですので、皆さん使ってみてはいかがかと思います。

ソースは[こちらのリポジトリ](https://github.com/Wreulicke/spring-sandbox/tree/e41b17b8a922cfef1b646d0728a39910263527bd)においています。

次は似たようなクラスのTestExecutionListenerで楽にしてみたいなぁとか思うわけですが。
いつになるやら。。。

さぁラーメン食べに行くぞー！！

## ぼやき

これは楽になったとは言えない。
