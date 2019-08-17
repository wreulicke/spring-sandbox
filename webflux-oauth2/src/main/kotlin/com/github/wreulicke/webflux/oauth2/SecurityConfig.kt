package com.github.wreulicke.webflux.oauth2

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import java.net.URI


@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    companion object {
        val log = LoggerFactory.getLogger(SecurityConfig::class.java)
    }

    @Bean
    fun getAuthenticationManager(): ReactiveAuthenticationManager {
        return CheckTokenReactiveAuthenticationManager(URI.create("https://<endpoint>/oauth/check_token"), "clientId", "clientSecret")
    }

    @Bean
    fun configure(http: ServerHttpSecurity): SecurityWebFilterChain {
        val authenticationManager = getAuthenticationManager()
        val oauth2 = AuthenticationWebFilter(authenticationManager)
        oauth2.setServerAuthenticationConverter(ServerBearerTokenAuthenticationConverter())
        http.addFilterAt(oauth2, SecurityWebFiltersOrder.AUTHENTICATION)
        return http
                .authorizeExchange()
                .anyExchange().authenticated()
                .and()
                .httpBasic().disable()
                .build();
    }

}
