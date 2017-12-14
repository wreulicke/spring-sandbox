はい、こんにちは、齋藤です。
どうしましょう。

今日は久しぶりに Springです。

今回の記事はSpringのトランザクションの管理方法について記事を書きます。

宣言的トランザクションについては、[Transaction Management](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/transaction.html)や
[Qiitaに書かれてある記事](https://qiita.com/NagaokaKenichi/items/a279857cc2d22a35d0dd) に大体のことは書かれていると思います。

宣言的トランザクションを使っている場合にこういう場合はどうしたら良いんだろう？というのを解決する方法について、ブログを書いていきます。

宣言的トランザクションがネストしている場合にどうするべきなのだろうか、という話で
メソッド2つ書くか、少し明示的にトランザクションを管理するかなぁ、という結論になりました。

## はじめに

皆さんは、どのようなコードを普段書かれているのか気になりますが
まずは素朴に宣言的トランザクションを使ったコードを書いてみましょう。


```java
class ServiceB{

    @Transactional
    public void doSomething(){
        // do something without any service dependency ...
    }

}
```

何も難しいことはないと思います。

ではもう一つサンプルを見てみましょう。

今度は　Serviceが複数あった場合に
A という Serviceが BというServiceを呼び出すようなケースです。
次のようなコードです。

```java
class ServiceA{

    @Transactional
    public void doSomething(){
        serviceB.doSomething();

        // do something...
    }
}

class ServiceB{

    @Transactional
    public void doSomething(){
        // do something without any service dependency ...
    }

}
```

示した通り、Service A では Service Bを呼び出しております。

ここまでは何も問題はありませんでした。

## 失敗したのは無視してデータベースに書き込みたい

先ほどServiceBのトランザクションが失敗した場合は ServiceAで始まったトランザクションも失敗することになります。
（これが良いかどうかは要件次第ではあるのですがひとまず置いておきます。）

では今度は次のように、例外が発生したのは無視してデータベースに書き込むような場合
どうなるでしょうか。

```java
class ServiceA {

    @Transactional
    public void doSomething(){
        try{
            serviceB.doSomething();
        } catch(Exception e){} // 失敗は見なかったことにする
        
        entityManager.persist(new User()); // ここで生成した新しいユーザは登録されて欲しい。

    }
}

class ServiceB {

    @Transactional
    public void doSomething(){
        // do something without any service dependency ...
    }

}
```

このようなコードを書いた場合、Springでは ServiceAのトランザクションが終了する際に例外が発生します。
なぜかというと、ServiceB で発生した例外によってトランザクションがロールバックオンリーになっているからです。

実際には次のようなコードに修正すれば 動くはずでしょう。


```java
class ServiceA {

    @Transactional
    public void doSomething(){
        try{
            serviceB.doSomething();
        } catch(Exception e){} // 失敗は見なかったことにする
        
        entityManager.persist(new User()); // ここで生成した新しいユーザは登録されて欲しい。

    }
}

class ServiceB {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doSomething(){
        // do something...
    }

}
```

では、このServiceBの修正は妥当でしょうか？
もちろん、ダメなケースが存在します。

## どういうケースでダメになるのか

アノテーションの属性を変えたところ
SerivceA --> ServiceB といった形で呼び出される場合は思ったように動くようになりました。

では、何がダメなのでしょうか
それは次のようなケースです。

```java
class ServiceA {

    @Transactional
    public void doSomething(){
        try{
            serviceB.doSomething();
        } catch(Exception e){} // 失敗は見なかったことにする
        
        entityManager.persist(new User()); // ここで生成した新しいユーザは登録されて欲しい。
    }
}

class ServiceB {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doSomething(){
        // do something...
    }

}

class ServiceC {

	@Transactional
    public void doSomething() {
        entityManager.persist(new User()); // ユーザ登録

        serviceB.doSomething();

        // do something...
        
        if (anyCondition) // 何らかの理由でロールバックしたい
            throw new RuntimeException();
    }

}
```

上記で示したように、もう一つ ServiceBを呼び出すような、ServiceC があった場合に挙動が変わります。
もう少し簡略化して次のようなコードを見てみます。

```java

class ServiceC {

	@Transactional
	public User doSomething() {
		User user1 = new User();
		user1.setName("testG");
		serviceB.doSomething(user1);

        // サンプルなので、どんなときも例外にした。
        // if (anyCondition) 
		    throw new RuntimeException();
	}
}

class ServiceB {

	// @Transactional // もともとこうだった
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public User doSomething(User user) {
		entityManager.persist(user);
        return user;
	}
}

```

上記例を動かすと分かるのですが
この場合、Transactionalアノテーションの propagation属性の値によって動きが変わります。

では、解決方法としてはどうするのが良いのでしょうか。

## 解決方法

結論としては、2つ思い浮かべました。

1. Transactionalアノテーションのpropagation属性だけが違うメソッドを追加する
2. TransactionTemplateを使って、ServiceB の外でトランザクションを分ける

1 は簡単です。アノテーションを変えた同じメソッドを用意しておくだけです。
実際の中の処理はまとめても良いかもしれません。

簡単ですね。

```java
class ServiceB {

    @Transactional
	public User doSomethingA(User user) {
		entityManager.persist(user);
        return user;
	}
    
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public User doSomethingB(User user) {
        doSomthingA(user)
	}
    
}
```

2 は少し大変かもしれません。最初の方のServiceAの例で書いてみます。

```java
@Slf4j
class ServiceA {
	
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
    ServiceB serviceB;

	@Transactional
	public void doSomething() {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		try {
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					serviceB.doSomething(user);
				}
			});
		} catch (Exception e) {
            log.error(e.getMessage(), e)
		}

        // serviceBで例外が起きても、ここでコミットして大丈夫
	}
}
```

これで、serviceBで例外が発生しても後続の処理でデータベースに書き込み等ができるようになります。
アノテーションで2つ用意するよりも明示的で良いとは思うのですが
いささか辛い感じもしますね・・・

全体のコードとしてはこんな感じ。

```java
@Slf4j
class ServiceA {
	
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
    ServiceB serviceB;

	@Transactional
	public void doSomething() {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		
		try {
			template.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					serviceB.doSomething(user);
				}
			});
		} catch (Exception e) {
            log.error(e.getMessage(), e)
		}

        // serviceBで例外が起きても、ここでコミットして大丈夫
	}
}

class ServiceB {

    @Transactional
    public void doSomething() {
        // do something
    }

}

class ServiceC {

    @Autowired
    ServiceB serviceB;

    @Transactional
    public void doSomething() {
        serviceB.doSomething();
    }
}
```

## まとめ

今回の記事では実際に動くコードは出しませんでしたが、宣言的トランザクションがネストしている場合にどうするべきなのだろうか、という話でした。
実際に書いたコードは[こちら](https://github.com/wreulicke/spring-sandbox/blob/96634e3ba0df990eb57b2326ff9fb46936651113/transaction-manager/src/main/java/com/github/wreulicke/spring/transaction/UserController.java)に置いてあります。

結論としてはメソッド2つ書くか
少し明示的にトランザクションを管理するという形になりました。

どちらにせよ、あまり良い手法ではないような気はしますが
やろうとしている内容の性質上、仕方ないのかなとも思いました。

また、今回のサンプルで示した書き方だと 外側でロールバックした場合 ( 記事では ServiceCの処理中 )、
ネストしたトランザクションがロールバックしません。 (記事中では ServiceBの処理 ) 
外側でロールバックした時にネストしている方もロールバックしたい場合はNestedとかを使うのかな？
`suke_masa` さんの[記事](http://masatoshitada.hatenadiary.jp/entry/2015/12/05/135825)を見ながら
この記事としては終わりにしておきます。

いかがだったでしょうか。
こんな書き方もあるよ、というのがある方は
コメントで教えていただけると幸いです。
