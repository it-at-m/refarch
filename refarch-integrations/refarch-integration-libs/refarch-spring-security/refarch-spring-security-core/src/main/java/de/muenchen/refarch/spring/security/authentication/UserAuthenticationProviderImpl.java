/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik der Landeshauptstadt München, 2020
 */

package de.muenchen.refarch.spring.security.authentication;

import static de.muenchen.refarch.spring.security.client.ClientParameters.fromEnvironment;

import de.muenchen.refarch.spring.security.PrincipalUtil;
import de.muenchen.refarch.spring.security.SecurityConfiguration;
import de.muenchen.refarch.spring.security.SpringSecurityProperties;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * User authentication provider. Extracts the username from the token.
 */
@Component
@Profile(SecurityConfiguration.SECURITY)
@Slf4j
public class UserAuthenticationProviderImpl implements UserAuthenticationProvider {

    public static final String NAME_UNAUTHENTICATED_USER = "unauthenticated";
    private final String userNameAttribute;

    public UserAuthenticationProviderImpl(SpringSecurityProperties springSecurityProperties, Environment environment) {
        this.userNameAttribute = fromEnvironment(environment, springSecurityProperties.getClientRegistration())
                .getUserNameAttribute();
    }

    @Override
    @NonNull
    public String getLoggedInUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AbstractAuthenticationToken && authentication.getPrincipal() instanceof Jwt jwt) {
            return (String) jwt.getClaims().get(userNameAttribute);
        }
        return NAME_UNAUTHENTICATED_USER;
    }

    @Override
    @NonNull
    public Set<String> getLoggedInUserRoles() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new HashSet<>(PrincipalUtil.extractRoles(authentication));
    }
}
