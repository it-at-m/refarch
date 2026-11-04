package de.muenchen.oss.refarch.gateway.configuration;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {
    private static final String API_ROUTES_PREFIX = "/api/**";
    private static final String CLIENT_ROUTES_PREFIX = "/clients/**";
    private static final String PUBLIC_ROUTES_PREFIX = "/public/**";

    private final CsrfProtectionMatcher csrfProtectionMatcher;
    private final SecurityProperties securityProperties;
    private final SessionTimeout sessionTimeout;

    @Bean
    @Order(0)
    public SecurityWebFilterChain clientAccessFilterChain(final ServerHttpSecurity http) {
        // build patterns
        final List<String> patternsList = new ArrayList<>();
        patternsList.add(CLIENT_ROUTES_PREFIX);
        if (!securityProperties.getClientPatterns().isEmpty()) {
            patternsList.addAll(securityProperties.getClientPatterns());
        }
        final String[] patterns = patternsList.toArray(new String[0]);
        // security config
        http.securityMatcher(ServerWebExchangeMatchers.pathMatchers(patterns))
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(HttpMethod.OPTIONS, patterns).permitAll()
                        .anyExchange().authenticated())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityWebFilterChain apiAccessFilterChain(final ServerHttpSecurity http) {
        return configureSessionSecurity(http)
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(API_ROUTES_PREFIX))
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain springSecurityFilterChain(final ServerHttpSecurity http) {
        return configureSessionSecurity(http).build();
    }

    private ServerHttpSecurity configureSessionSecurity(final ServerHttpSecurity http) {
        return http
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> {
                    // permitAll
                    authorizeExchangeSpec.pathMatchers(HttpMethod.OPTIONS, API_ROUTES_PREFIX).permitAll();
                    authorizeExchangeSpec.pathMatchers(
                            "/api/*/actuator/info",
                            "/actuator/health",
                            "/actuator/health/liveness",
                            "/actuator/health/readiness",
                            "/actuator/info",
                            "/actuator/metrics",
                            "/actuator/sbom",
                            "/actuator/sbom/application",
                            PUBLIC_ROUTES_PREFIX)
                            .permitAll();
                    // dynamic permitAll from properties
                    this.applyDynamicPermitAll(authorizeExchangeSpec);
                    // only authenticated
                    authorizeExchangeSpec.anyExchange().authenticated();
                })
                .csrf(csrfSpec -> {
                    /*
                     * Custom csrf request handler for spa and BREACH attack protection.
                     * https://docs.spring.io/spring-security/reference/6.1-SNAPSHOT/servlet/exploits/csrf.html#csrf-
                     * integration-javascript-spa
                     */
                    csrfSpec.csrfTokenRequestHandler(new SpaServerCsrfTokenRequestHandler());
                    /*
                     * The necessary subscription for csrf token attachment to {@link ServerHttpResponse}
                     * is done in class {@link CsrfTokenAppendingHelperFilter}.
                     */
                    csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
                    csrfSpec.requireCsrfProtectionMatcher(csrfProtectionMatcher);
                })
                .oauth2Login(oAuth2LoginSpec -> oAuth2LoginSpec.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler() {
                    @Override
                    @NonNull public Mono<Void> onAuthenticationSuccess(@NonNull final WebFilterExchange webFilterExchange,
                            @NonNull final Authentication authentication) {
                        return webFilterExchange.getExchange().getSession()
                                .flatMap(webSession -> {
                                    if (sessionTimeout.getTimeout() != null) {
                                        webSession.setMaxIdleTime(sessionTimeout.getTimeout());
                                    }
                                    return super.onAuthenticationSuccess(webFilterExchange, authentication);
                                });
                    }
                }));
    }

    /// Apply dynamic permitAll rules based on properties.
    private void applyDynamicPermitAll(final ServerHttpSecurity.AuthorizeExchangeSpec authorize) {
        for (final SecurityProperties.PermitRule rule : securityProperties.getPublicPatterns()) {
            for (final HttpMethod method : rule.getMethods()) {
                authorize.pathMatchers(method, rule.getPattern()).permitAll();
            }
        }
    }
}
