package de.muenchen.refarch.integration.s3.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utilities for authentication data.
 */
public class AuthUtils {

    public static final String NAME_UNAUTHENTICATED_USER = "unauthenticated";

    private static final String TOKEN_USER_NAME = "user_name";

    private AuthUtils() {
    }

    /**
     * Extracts the username from the existing Spring Security Context via
     * {@link SecurityContextHolder}.
     *
     * @return the username or "unauthenticated", if no {@link Authentication} exists
     */
    public static String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return (String) jwtAuth.getTokenAttributes().getOrDefault(TOKEN_USER_NAME, null);
        } else if (authentication instanceof UsernamePasswordAuthenticationToken usernameAuth) {
            return usernameAuth.getName();
        } else {
            return NAME_UNAUTHENTICATED_USER;
        }
    }
}
