package com.github.wreulicke.webflux.oauth2

import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import com.nimbusds.oauth2.sdk.id.Audience
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
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
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class OAuth2IntrospectionReactiveAuthenticationManager(
        val introspectionUri: URI,
        val webClient: WebClient
) : ReactiveAuthenticationManager {
    companion object {
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
                .cast(Authentication::class.java)
    }

    private fun authenticate(token: String): Mono<OAuth2IntrospectionAuthenticationToken> {
        return introspect(token)
                .map { response ->
                    val claims = convertClaimsSet(response)
                    claims.entries.stream().forEach { SecurityConfig.log.info("key: {}, value:{}, class:{}", it.key, it.value, it.javaClass) }
                    val iat = claims.get(OAuth2IntrospectionClaimNames.ISSUED_AT) as Instant?
                    val exp = claims.get(OAuth2IntrospectionClaimNames.EXPIRES_AT) as Instant?

                    // construct token
                    val accessToken = OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token, iat, exp)
                    val authorities = extractAuthorities(claims)
                    OAuth2IntrospectionAuthenticationToken(accessToken, claims, authorities)
                }
    }

    fun extractAuthorities(claims: Map<String, Any>): Collection<GrantedAuthority> {
        val scopes = claims.get(OAuth2IntrospectionClaimNames.SCOPE) as Collection<String>?;

        return Optional.ofNullable(scopes)
                .orElse(Collections.emptyList())
                .stream()
                .map { SimpleGrantedAuthority("SCOPE_$it") }
                .collect(Collectors.toList());

    }


    fun convertClaimsSet(response: TokenIntrospectionSuccessResponse): Map<String, Any> {
        val claims: MutableMap<String, Any> = response.toJSONObject()
        if (response.getAudience() != null) {
            val audience: List<String> = response.getAudience().stream()
                    .map(Audience::getValue).collect(Collectors.toList());
            claims.put(OAuth2IntrospectionClaimNames.AUDIENCE, Collections.unmodifiableList(audience));
        }
        if (response.getClientID() != null) {
            claims.put(OAuth2IntrospectionClaimNames.CLIENT_ID, response.getClientID().getValue());
        }
        if (response.getExpirationTime() != null) {
            val exp = response.getExpirationTime().toInstant();
            claims.put(OAuth2IntrospectionClaimNames.EXPIRES_AT, exp);
        }
        if (response.getIssueTime() != null) {
            val iat = response.issueTime.toInstant();
            claims.put(OAuth2IntrospectionClaimNames.ISSUED_AT, iat);
        }
        if (response.getIssuer() != null) {
            claims.put(OAuth2IntrospectionClaimNames.ISSUER, issuer(response.getIssuer().getValue()));
        }
        if (response.getNotBeforeTime() != null) {
            claims.put(OAuth2IntrospectionClaimNames.NOT_BEFORE, response.getNotBeforeTime().toInstant());
        }
        if (response.getScope() != null) {
            claims.put(OAuth2IntrospectionClaimNames.SCOPE, Collections.unmodifiableList(response.getScope().toStringList()));
        }

        return claims;
    }

    fun introspect(token: String): Mono<TokenIntrospectionSuccessResponse> {
        return Mono.just(token)
                .flatMap(this::makeRequest)
                .flatMap(this::adaptToNimbusResponse)
                .map(this::parseNimbusResponse)
                .map(this::castToNimbusSuccess)
                /* relying solely on the authorization server to validate this token (not checking 'exp', for example) */
                .doOnNext { response -> validate(token, response) }
                .onErrorMap(
                        { e ->
                            SecurityConfig.log.error("aaa", e)
                            e !is OAuth2AuthenticationException
                        },
                        this::onError)
    }


    fun makeRequest(token: String): Mono<ClientResponse> {
        return this.webClient.post()
                .uri(this.introspectionUri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(BodyInserters.fromFormData("token", token))
                .exchange()
    }


    private fun adaptToNimbusResponse(responseEntity: ClientResponse): Mono<HTTPResponse> {
        val response = HTTPResponse(responseEntity.rawStatusCode())
        response.setHeader(HttpHeaders.CONTENT_TYPE, responseEntity.headers().contentType().get().toString())
        if (response.statusCode != HTTPResponse.SC_OK) {
            throw OAuth2AuthenticationException(
                    invalidToken("Introspection endpoint responded with " + response.statusCode))
        }
        return responseEntity.bodyToMono(String::class.java)
                .doOnNext {
                    response.content = it
                }
                .map { response }
    }

    private fun parseNimbusResponse(response: HTTPResponse): TokenIntrospectionResponse {
        try {
            var response = TokenIntrospectionResponse.parse(response)
            SecurityConfig.log.info("{}", response)
            return response
        } catch (ex: Exception) {
            throw OAuth2AuthenticationException(invalidToken(ex.message), ex)
        }
    }

    private fun castToNimbusSuccess(introspectionResponse: TokenIntrospectionResponse): TokenIntrospectionSuccessResponse {
        if (!introspectionResponse.indicatesSuccess()) {
            throw OAuth2AuthenticationException(invalidToken("Token introspection failed"))
        }
        return introspectionResponse as TokenIntrospectionSuccessResponse
    }

    private fun validate(token: String, response: TokenIntrospectionSuccessResponse) {
        if (!response.isActive) {
            throw OAuth2AuthenticationException(invalidToken("Provided token [$token] isn't active"))
        }
    }

    private fun issuer(uri: String): URL {
        try {
            return URL(uri);
        } catch (ex: Exception) {
            throw OAuth2AuthenticationException(
                    invalidToken("Invalid iss value: " + uri), ex);
        }
    }

    private fun invalidToken(message: String?): BearerTokenError {
        return BearerTokenError("invalid_token",
                HttpStatus.UNAUTHORIZED, message,
                "https://tools.ietf.org/html/rfc7662#section-2.2");
    }


    private fun onError(e: Throwable): OAuth2AuthenticationException {
        SecurityConfig.log.error("onError", e)
        val invalidToken = invalidToken(e.message);
        return OAuth2AuthenticationException(invalidToken, e.message);
    }
}
