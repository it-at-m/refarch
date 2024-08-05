/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik der Landeshauptstadt München, 2020
 */
package de.muenchen.refarch.spring.security.authentication;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

/**
 * This filter logs the username for requests.
 */
@Component
@Order(1)
@Slf4j
public class RequestResponseLoggingFilter implements Filter {

    private static final String REQUEST_LOGGING_MODE_ALL = "all";

    private static final String REQUEST_LOGGING_MODE_CHANGING = "changing";

    private static final List<String> CHANGING_METHODS = Arrays.asList("POST", "PUT", "PATCH", "DELETE");

    /**
     * The property or a zero length string if no property is available.
     */

    private final String requestLoggingMode;
    private final UserAuthenticationProvider userAuthenticationProvider;

    public RequestResponseLoggingFilter(UserAuthenticationProvider userAuthenticationProvider,
            @Value("${security.logging.requests:}") String requestLoggingMode) {
        this.userAuthenticationProvider = userAuthenticationProvider;
        this.requestLoggingMode = requestLoggingMode;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    /**
     * The method logs the username extracted out of the {@link SecurityContext}. In addition to the username, the kind of HTTP-Request and the targeted URI is
     * logged.
     * <p>
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (this.checkForLogging(httpRequest)) {
            log.info("User {} executed {} on URI {} with http status {}",
                    userAuthenticationProvider.getLoggedInUser(),
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus());
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    /**
     * The method checks if logging the username should be done.
     *
     * @param httpServletRequest The request to check for logging.
     * @return True if logging should be done otherwise false.
     */
    private boolean checkForLogging(final HttpServletRequest httpServletRequest) {
        switch (this.requestLoggingMode) {
        case REQUEST_LOGGING_MODE_ALL:
            return true;
        case REQUEST_LOGGING_MODE_CHANGING:
            return CHANGING_METHODS.contains(httpServletRequest.getMethod());
        default:
            return false;
        }
    }
}
