package com.github.wreulicke.webflux.oauth2;

import static com.github.wreulicke.webflux.oauth2.OAuth2IntrospectionClaimNames.SUBJECT;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.util.Assert;

public class OAuth2IntrospectionAuthenticationToken
		extends AbstractOAuth2TokenAuthenticationToken<OAuth2AccessToken> {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	private Map<String, Object> attributes;
	private String name;

	/**
	 * Constructs a {@link OAuth2IntrospectionAuthenticationToken} with the provided arguments
	 *
	 * @param token The verified token
	 * @param authorities The authorities associated with the given token
	 */
	public OAuth2IntrospectionAuthenticationToken(OAuth2AccessToken token,
			Map<String, Object> attributes, Collection<? extends GrantedAuthority> authorities) {

		this(token, attributes, authorities, null);
	}

	/**
	 * Constructs a {@link OAuth2IntrospectionAuthenticationToken} with the provided arguments
	 *
	 * @param token The verified token
	 * @param authorities The authorities associated with the given token
	 * @param name The name associated with this token
	 */
	public OAuth2IntrospectionAuthenticationToken(OAuth2AccessToken token,
		Map<String, Object> attributes, Collection<? extends GrantedAuthority> authorities, String name) {
		super(token, authorities);
		Assert.notEmpty(attributes, "attributes cannot be empty");
		this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
		this.name = name == null ? (String) attributes.get(SUBJECT) : name;
		setAuthenticated(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getTokenAttributes() {
		return this.attributes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return this.name;
	}
}
