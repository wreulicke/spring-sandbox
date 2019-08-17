package com.github.wreulicke.webflux.oauth2

import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.BearerTokenError
import org.springframework.util.StringUtils
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.*

class CheckTokenReactiveAuthenticationManager(
        val checkToken: URI,
        val webClient: WebClient
) : ReactiveAuthenticationManager {

    val caches = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build<String, Authentication>()


    companion object {

        val log = LoggerFactory.getLogger(CheckTokenReactiveAuthenticationManager::class.java)

        fun basicHeaderValue(clientId: String, clientSecret: String): String {
            var headerValue = "$clientId:"
            if (StringUtils.hasText(clientSecret)) {
                headerValue += clientSecret
            }
            return "Basic " + Base64.getEncoder().encodeToString(headerValue.toByteArray(StandardCharsets.UTF_8))
        }
    }

    constructor(introspectionUri: URI, clientId: String, clientSecret: String) : this(introspectionUri, webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.AUTHORIZATION, basicHeaderValue(clientId, clientSecret))
            .build())

    override fun authenticate(authentication: Authentication?): Mono<Authentication> {
        return Mono.justOrEmpty(authentication)
                .filter(BearerTokenAuthenticationToken::class.java::isInstance)
                .cast(BearerTokenAuthenticationToken::class.java)
                .map(BearerTokenAuthenticationToken::getToken)
                .flatMap(this::authenticate)
    }

    private fun authenticate(token: String): Mono<Authentication> {
        return Mono.justOrEmpty(caches.getIfPresent(token))
                .onErrorResume {
                    checkToken(token)
                            .flatMap { response ->
                                response.bodyToMono(java.util.HashMap::class.java)
                                        .map { it as Map<String, Any> }
                            }
                            .map { map ->
                                if (map.containsKey("error")) {
                                    throw OAuth2AuthenticationException(
                                            invalidToken("contains error: " + map.get("error")))
                                }
                                val accessToken = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token, null, null)
                                val authorities = map.getOrElse("authorities") { emptyList<String>() } as List<String>
                                val claims = mutableMapOf<String, Any?>()
                                claims.put("exp", map.get("exp"))
                                claims.put("client_id", map.get("client_id"))
                                claims.put("scope", map.get("scope"))
                                claims.put("active", map.get("active"))
                                val a = OAuth2IntrospectionAuthenticationToken(accessToken, claims, authorities.map { SimpleGrantedAuthority(it) })
                                caches.put(token, a)
                                a

                            }
                            .cast(Authentication::class.java)
                            .onErrorMap(
                                    { e ->
                                        e !is OAuth2AuthenticationException
                                    },
                                    this::onError)
                }
    }


    fun checkToken(token: String): Mono<ClientResponse> {
        var body = this.webClient.post()
                .uri(this.checkToken)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(BodyInserters.fromFormData("token", token))
        log.info("header: {}", body.header("Authorization"))
        return body.exchange()
    }

    private fun invalidToken(message: String?): BearerTokenError {
        return BearerTokenError("invalid_token",
                HttpStatus.UNAUTHORIZED, message,
                "https://tools.ietf.org/html/rfc7662#section-2.2");
    }

    private fun onError(e: Throwable): OAuth2AuthenticationException {
        log.error("onError", e)
        val invalidToken = invalidToken(e.message);
        return OAuth2AuthenticationException(invalidToken, e.message);
    }
}
