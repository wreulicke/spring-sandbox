/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.simple;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.distribution.Version.v5_7_latest;

import java.io.IOException;

import org.apache.tomcat.jdbc.pool.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;

@Configuration
@ImportAutoConfiguration
@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
public class TestDatabaseConfiguration {

  @Bean(destroyMethod = "stop")
  EmbeddedMysql embeddedMysql() throws IOException {
    MysqldConfig config = new MysqldConfig.Builder(v5_7_latest).withFreePort()
      .build();
    return anEmbeddedMysql(config).addSchema("test")
      .start();
  }

  @Bean
  @Primary
  DataSourceProperties dataSourceProperties() throws IOException {
    MysqldConfig config = embeddedMysql().getConfig();
    DataSourceProperties dataSourceProperties = new DataSourceProperties();
    dataSourceProperties.setType(DataSource.class);
    dataSourceProperties.setUrl("jdbc:mysql://localhost:" + config.getPort() + "/test");
    dataSourceProperties.setDriverClassName("com.mysql.jdbc.Driver");
    dataSourceProperties.setUsername(config.getUsername());
    dataSourceProperties.setPassword(config.getPassword());
    return dataSourceProperties;
  }

}
