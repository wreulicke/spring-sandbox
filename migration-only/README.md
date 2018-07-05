
こんにちは。齋藤です。
今回はSpring Bootでマイグレーションだけ実行する方法を検討します。

## はじめに

Spring BootでFlywayをお使いの皆さん、起動時にマイグレーションする時間が長いと辛いですよね。

そこで、Webサーバとして立ち上げずにマイグレーションが動かせるか試してみます。

今回の記事の流れは以下の形です。

* マイグレーションを書く
* 起動する。（今回はgradleから立ち上げます）

今回はデータベースのマイグレーションのツールとして、flywayを使います。

## マイグレーションをなんかいい感じで書きます

書けました。自分で書いてる適当なexampleからコピペです。

```sql
CREATE TABLE users (
    id BIGINT not null AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(128) NOT NULL,
	password VARCHAR(128) NOT NULL,
	UNIQUE INDEX `username_UNIQUE` (`username`)
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;

CREATE TABLE user_authorities (
	username VARCHAR(128) PRIMARY KEY,
	authorities VARCHAR(128) NOT NULL
)/*! CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
```

## build.gradleをspring boot向けにちょこちょこ書いていきます。

Minimalな感じでSpring Bootが起動しそうなbuild.gradleを抜粋してきました。
本来使っている設定は[こちらのリポジトリ](https://github.com/wreulicke/spring-sandbox/tree/master/migration-only)をご覧ください。

```groovy
buildscript {
  ext {
    springBootVersion = "1.5.14.RELEASE"
  }
  repositories {
    jcenter()
    maven { url "https://plugins.gradle.org/m2/" }
  }
  dependencies {
    classpath "org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion"
    classpath "io.spring.gradle:dependency-management-plugin:1.0.4.RELEASE"
  }
}

apply plugin: "java"
apply plugin: "org.springframework.boot"
apply plugin: "io.spring.dependency-management"

dependencies {
  compile "org.springframework.boot:spring-boot-starter-actuator"
  compile "org.springframework.boot:spring-boot-starter-data-jpa"
  compile "org.springframework.boot:spring-boot-starter-web"
  compile "org.springframework.boot:spring-boot-starter-security"
  compile "org.springframework.boot:spring-boot-starter-aop"
  compile "org.springframework.boot:spring-boot-devtools"

  annotationProcessor "org.springframework.boot:spring-boot-configuration-processor"

  compile "com.zaxxer:HikariCP:2.7.8"

  compile "com.fasterxml.jackson.module:jackson-module-parameter-names"
  compile "com.fasterxml.jackson.datatype:jackson-datatype-jdk8"
  compile "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
  compile "org.zalando:logbook-spring-boot-starter:1.8.1"

  compile "org.flywaydb:flyway-core" // flywayを使います。

  runtime "org.springframework.boot:spring-boot-devtools"
  compile "mysql:mysql-connector-java"
}
```

## サンプルとしてgradleからbootRunするタスクを書く

本来 bootRepackage したjarで試すのが筋だとは思います。
ちなみに、Spring Boot2系ではbootJarに名前が変わっています。設定のマイグレーションをやりましょう。

今回は簡略化のために Spring BootのGradle PluginのBootRunタスクを使います。

理由としては、今度見た時に僕が忘れないようにですね。（最近歳なので忘れ物が多い）
コマンドライン引数から `spring.main.web-environment` をfalseにして起動します。

```groovy
task bootRunOnlyMigration(type: org.springframework.boot.gradle.run.BootRunTask) { // spring boot 2系から名前がBootRunに変わるはずです。
  main "com.github.wreulicke.simple.SimpleApplication"
  classpath = sourceSets.main.runtimeClasspath
  args = [
          "--spring.main.web-environment=false", // ここ重要
          "--spring.datasource.driver-class-name=com.mysql.jdbc.Driver",
          "--spring.datasource.url=jdbc:mysql://127.0.0.1:3306/test",
          "--spring.datasource.username=root",
          "'--spring.datasource.password='"
  ]
}
```

書けました。

## データベースのマイグレーションだけアプリケーションを起動して動かしてみる

動かしてみます。

```bash
$ ./gradlew bootRunOnlyMigration
2018-06-21 08:36:29.249  INFO 52280 --- [  restartedMain] o.f.c.i.dbsupport.DbSupportFactory       : Database: jdbc:mysql://127.0.0.1:3306/test (MySQL 5.7)
2018-06-21 08:36:29.300  INFO 52280 --- [  restartedMain] o.f.core.internal.command.DbValidate     : Validated 1 migration (execution time 00:00.018s)
# マイグレーションが動いてそう
2018-06-21 08:36:29.372  INFO 52280 --- [  restartedMain] o.f.c.i.metadatatable.MetaDataTableImpl  : Creating Metadata table: `test`.`schema_version`
2018-06-21 08:36:29.576  INFO 52280 --- [  restartedMain] o.f.core.internal.command.DbMigrate      : Current version of schema `test`: Empty Schema
2018-06-21 08:36:29.577  INFO 52280 --- [  restartedMain] o.f.core.internal.command.DbMigrate      : Migrating schema `test` to version 1 - create initial tables
2018-06-21 08:36:29.676  INFO 52280 --- [  restartedMain] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema `test` (execution time 00:00.307s).
# マイグレーションが動いてそう
2018-06-21 08:32:32.742  INFO 51726 --- [  restartedMain] o.s.c.support.DefaultLifecycleProcessor  : Starting beans in phase 0
2018-06-21 08:32:32.856  INFO 51726 --- [  restartedMain] c.g.wreulicke.simple.SimpleApplication   : Started SimpleApplication in 9.089 seconds (JVM running for 9.867)
2018-06-21 08:32:32.858  INFO 51726 --- [       Thread-8] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@2ffaffe4: startup date
[Thu Jun 21 08:32:24 JST 2018]; root of context hierarchy
2018-06-21 08:32:32.862  INFO 51726 --- [       Thread-8] o.s.c.support.DefaultLifecycleProcessor  : Stopping beans in phase 0
2018-06-21 08:32:32.866  INFO 51726 --- [       Thread-8] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
2018-06-21 08:32:32.866  INFO 51726 --- [       Thread-8] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans
2018-06-21 08:32:32.909  INFO 51726 --- [       Thread-8] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2018-06-21 08:32:32.912  INFO 51726 --- [       Thread-8] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2018-06-21 08:32:32.915  INFO 51726 --- [       Thread-8] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.

BUILD SUCCESSFUL in 16s
```

というわけで起動しました。
データベースのマイグレーションが動きました。

## まとめ

Spring Boot 1系でデータベースのマイグレーションだけ実行する方法を紹介しました。
今回の記事では `spring.main.web-environment`をfalseにして起動することによって
アプリケーションがすぐに終了する形になっています。（マイグレーションがすぐ終わるとは言っていない）

本当はちゃんとCommandLineRunnerとかを使ってコマンドを実装するんでしょうか？
ひとまず用途は満たせそうですが。。。

また、今回は簡単なアプリケーションだったので良かったのですが
バックグラウンドで動くような処理がある場合は
`@ConditionalOnWebApplication` などを使ってBeanの初期化を制御する必要があると思います。

では、また次の記事でお会いしましょう。