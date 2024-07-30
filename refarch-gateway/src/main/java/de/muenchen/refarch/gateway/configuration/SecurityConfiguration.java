package de.muenchen.refarch.gateway.configuration;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@Profile("!no-security")
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CsrfProtectionMatcher csrfProtectionMatcher;

    /**
     * Same lifetime as SSO Session (e.g. 10 hours).
     */
    @Value("${spring.session.timeout:36000}")
    private long springSessionTimeoutSeconds;

    @Bean
    @Order(0)
    public SecurityWebFilterChain clientAccessFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/clients/**"))
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(HttpMethod.OPTIONS, "/clients/**").permitAll()
                        .anyExchange().authenticated())
                .cors(corsSpec -> {
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> {
                    // permitAll
                    authorizeExchangeSpec.pathMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                            .pathMatchers("/api/*/info",
                                    "/actuator/health",
                                    "/actuator/health/liveness",
                                    "/actuator/health/readiness",
                                    "/actuator/info",
                                    "/actuator/metrics")
                            .permitAll()
                            .pathMatchers(HttpMethod.OPTIONS, "/public/**").permitAll()
                            .pathMatchers(HttpMethod.GET, "/public/**").permitAll()
                            // only authenticated
                            .anyExchange().authenticated();
                })
                .cors(corsSpec -> {
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
                    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
                        webFilterExchange.getExchange().getSession().subscribe(
                                webSession -> webSession.setMaxIdleTime(Duration.ofSeconds(springSessionTimeoutSeconds)));
                        return super.onAuthenticationSuccess(webFilterExchange, authentication);
                    }
                }));

        return http.build();
    }

}
