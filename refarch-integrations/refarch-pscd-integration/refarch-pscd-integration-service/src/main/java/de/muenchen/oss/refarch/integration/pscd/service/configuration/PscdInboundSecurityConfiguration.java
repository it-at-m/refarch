package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

/**
 * HTTP Basic in front of the inbound HTTP channels, configured under
 * {@code refarch.pscd.inbound.security}.
 *
 * <p>
 * One account, held in memory, shared by the SOAP and REST edges. The sending systems are a handful
 * of known machines, so a user store would be ceremony without benefit. The file channel is not
 * affected; it has no HTTP surface.
 * </p>
 *
 * <p>
 * Submitting a batch needs the credential; reading the contract does not. The WSDL, the canonical
 * schema and the OpenAPI document stay open, so a sending system can be built and its tooling
 * pointed
 * at the service before any account exists.
 * </p>
 *
 * <p>
 * There is no switch to turn this off, and no default account: every environment configures its own
 * username and password, and a service without them refuses to start.
 * </p>
 *
 * <p>
 * Two deliberate departures from the browser-oriented defaults: CSRF protection is off, and no
 * session is created. Both edges are machine-to-machine, so there is no session to fix and no
 * cookie to ride, while leaving CSRF on would reject every POST without a token, which is every
 * POST a SOAP or REST sender makes.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class PscdInboundSecurityConfiguration {

    /** Shown in the {@code WWW-Authenticate} header of a rejected request. */
    private static final String REALM = "PSCD inbound";

    private final PscdInboundProperties properties;

    /**
     * @param soapPath where the Spring WS servlet is mapped, read from the property Boot maps it with,
     *            so the contract artefacts stay readable wherever the SOAP edge is moved to
     */
    @Bean
    public SecurityFilterChain pscdInboundSecurityFilterChain(final HttpSecurity http,
            @Value("${spring.webservices.path:/services}") final String soapPath,
            final PscdInboundAuthenticationLog authenticationLog) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        // The contract is public: the WSDL, the schema it includes and the OpenAPI
                        // document are what a sending system is built against, and tooling fetches
                        // them before anyone has credentials. Only GET is opened, and submitting is
                        // POST on both edges: the SOAP address doubles as the WSDL address, so the
                        // method is what separates reading the contract from using it.
                        .requestMatchers(HttpMethod.GET, soapPath, soapPath + "/**",
                                "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml",
                                "/swagger-ui.html", "/swagger-ui/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(loggingEntryPoint(authenticationLog)))
                .build();
    }

    /**
     * The {@code 401} response, with the rejection logged on the way out.
     *
     * <p>
     * Wraps the standard Basic entry point rather than replacing it, so the {@code WWW-Authenticate}
     * header and realm are unchanged; the wrapper exists because a request without an
     * {@code Authorization} header never reaches an authentication provider and so publishes no event
     * for {@code PscdInboundAuthenticationLog} to hear.
     * </p>
     */
    private static AuthenticationEntryPoint loggingEntryPoint(final PscdInboundAuthenticationLog authenticationLog) {
        final BasicAuthenticationEntryPoint basic = new BasicAuthenticationEntryPoint();
        basic.setRealmName(REALM);
        basic.afterPropertiesSet();
        return (request, response, authenticationException) -> {
            authenticationLog.rejectedWithoutCredentials(request);
            basic.commence(request, response, authenticationException);
        };
    }

    /**
     * The single account the credentials describe. Declared here rather than left to Spring Boot, whose
     * fallback would be a user with a password generated on every start and printed to the log.
     *
     * @throws IllegalStateException if either credential is missing. There is deliberately no default
     *             to fall back to: a shipped credential is one everybody knows, and an open service or
     *             one nobody can call are both worse than a start that stops and says why
     */
    @Bean
    public UserDetailsService pscdInboundUserDetailsService() {
        final PscdInboundProperties.SecurityProperties security = this.properties.getSecurity();
        if (isBlank(security.getUsername()) || isBlank(security.getPassword())) {
            throw new IllegalStateException(
                    "refarch.pscd.inbound.security.username and .password must both be set: the inbound HTTP channels "
                            + "always require HTTP Basic and there is no default account. Supply them for this "
                            + "environment, e.g. REFARCH_PSCD_INBOUND_SECURITY_USERNAME and "
                            + "REFARCH_PSCD_INBOUND_SECURITY_PASSWORD.");
        }
        log.info("PSCD inbound HTTP channels require HTTP Basic as user '{}'", security.getUsername());
        return new InMemoryUserDetailsManager(User.withUsername(security.getUsername())
                .password(encoded(security.getPassword()))
                .authorities("ROLE_PSCD_SENDER")
                .build());
    }

    /**
     * Let the configured password be either a plain one or an already-encoded value: Spring Security
     * reads the {@code {id}} prefix to pick its encoder, so a value that has none is marked
     * {@code {noop}} and compared literally.
     */
    private static String encoded(final String password) {
        return password.startsWith("{") ? password : "{noop}" + password;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
