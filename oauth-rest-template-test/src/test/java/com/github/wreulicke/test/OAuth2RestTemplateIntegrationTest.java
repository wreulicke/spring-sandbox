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
package com.github.wreulicke.test;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.HttpServerErrorException;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class OAuth2RestTemplateIntegrationTest {

  @Rule
  public WireMockRule rule = new WireMockRule(wireMockConfig().dynamicPort()
    .dynamicHttpsPort());

  @Test
  public void testGetAccessToken_tokenEndpointIsServiceUnavailable() {
    stubFor(post(urlEqualTo("/oauth/token")).willReturn(serviceUnavailable()));

    ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
    resource.setAccessTokenUri("http://localhost:" + rule.port() + "/oauth/token");
    resource.setClientId("test");
    resource.setClientSecret("test-secret");
    OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource);
    assertThatThrownBy(oAuth2RestTemplate::getAccessToken).isInstanceOf(OAuth2AccessDeniedException.class)
      .hasCauseInstanceOf(HttpServerErrorException.class);
  }

  @Test
  public void testGetForEntity_tokenEndpointIsServiceUnavailable() {
    stubFor(post(urlEqualTo("/oauth/token")).willReturn(serviceUnavailable()));

    ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
    resource.setAccessTokenUri("http://localhost:" + rule.port() + "/oauth/token");
    resource.setClientId("test");
    resource.setClientSecret("test-secret");
    OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource);
    assertThatThrownBy(() -> oAuth2RestTemplate.getForEntity("http://localhost:" + rule.port() + "/test", String.class)).hasCauseInstanceOf(
      HttpServerErrorException.class);
  }

  @Test
  public void testGetForEntity_endpointIsServiceUnavailable() {
    stubFor(post(urlEqualTo("/oauth/token")).willReturn(okJson("{\"access_token\": \"test\"}")));
    stubFor(get(urlEqualTo("/test")).willReturn(serviceUnavailable()));

    ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
    resource.setAccessTokenUri("http://localhost:" + rule.port() + "/oauth/token");
    resource.setClientId("test");
    resource.setClientSecret("test-secret");
    OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource);
    assertThatThrownBy(() -> oAuth2RestTemplate.getForEntity("http://localhost:" + rule.port() + "/test", String.class)).isInstanceOf(
      HttpServerErrorException.class);
  }


}
