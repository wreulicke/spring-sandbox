package com.github.wreulicke.flexypool.demo

import com.vladmihalcea.flexypool.FlexyPoolDataSource
import com.vladmihalcea.flexypool.adaptor.TomcatCPPoolAdapter
import com.vladmihalcea.flexypool.metric.AbstractMetrics
import com.vladmihalcea.flexypool.metric.micrometer.MicrometerHistogram
import com.vladmihalcea.flexypool.metric.micrometer.MicrometerTimer
import com.vladmihalcea.flexypool.strategy.IncrementPoolOnTimeoutConnectionAcquiringStrategy
import io.micrometer.core.instrument.Metrics
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DatabaseDriver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableConfigurationProperties(DataSourceProperties::class)
class FlexyPoolConfig(val properties: DataSourceProperties) {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.tomcat")
    fun dataSource(): DataSource {
        val dataSource: DataSource = properties.initializeDataSourceBuilder()
                .type(DataSource::class.java)
                .build() as DataSource
        val databaseDriver = DatabaseDriver
                .fromJdbcUrl(properties.determineUrl())
        val validationQuery = databaseDriver.validationQuery
        if (validationQuery != null) {
            dataSource.setTestOnBorrow(true)
            dataSource.setValidationQuery(validationQuery)
        }
        return dataSource
    }

    @Bean
    fun configuration(): com.vladmihalcea.flexypool.config.Configuration<DataSource>? {
        return com.vladmihalcea.flexypool.config.Configuration.Builder<DataSource>(
                "test", dataSource(), TomcatCPPoolAdapter.FACTORY)
                .build()
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    fun pool(): FlexyPoolDataSource<DataSource> = FlexyPoolDataSource(configuration(),
            IncrementPoolOnTimeoutConnectionAcquiringStrategy.Factory(5)
        )
}

// for test
class EnhancedMicrometerMetrics(configurationProperties: com.vladmihalcea.flexypool.common.ConfigurationProperties<*, *, *>?, private val database: String)
    : AbstractMetrics(configurationProperties) {

    override fun stop() {
    }

    override fun timer(name: String): MicrometerTimer = MicrometerTimer(Metrics.globalRegistry.timer(name, "database", database))

    override fun start() {
    }

    override fun histogram(name: String) = MicrometerHistogram(Metrics.globalRegistry.summary(name, "database", database))
}
