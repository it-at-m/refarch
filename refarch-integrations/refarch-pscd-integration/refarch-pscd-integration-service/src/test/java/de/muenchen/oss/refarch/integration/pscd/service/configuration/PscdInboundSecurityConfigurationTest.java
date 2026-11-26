package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * The account behind the inbound HTTP Basic.
 *
 * <p>
 * There is no default one, so the interesting behaviour is what happens when a deployment forgets
 * to
 * configure it. Asserted on the bean method directly: a context that fails to start makes for a far
 * less readable test than the exception itself.
 * </p>
 */
class PscdInboundSecurityConfigurationTest {

    private static final String USERNAME = "pscd-sender";
    private static final String PASSWORD = "s3cret";

    @Test
    void buildsTheAccountFromTheConfiguredCredentials() {
        final UserDetailsService users = configuration(USERNAME, PASSWORD).pscdInboundUserDetailsService();

        assertThat(users.loadUserByUsername(USERNAME))
                .satisfies(user -> {
                    // Plain text is marked {noop}, which is how Spring Security is told to compare it as-is.
                    assertThat(user.getPassword()).isEqualTo("{noop}" + PASSWORD);
                    assertThat(user.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_PSCD_SENDER");
                });
    }

    /** An already-hashed value is kept, so a deployment need not hold the plaintext. */
    @Test
    void keepsAnAlreadyEncodedPassword() {
        final String encoded = "{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoOa1u0GDsKe6Bp3IuvBFBhAaS3nJ5Nl2u";

        final UserDetailsService users = configuration(USERNAME, encoded).pscdInboundUserDetailsService();

        assertThat(users.loadUserByUsername(USERNAME).getPassword()).isEqualTo(encoded);
    }

    @Test
    void refusesToStartWithoutAUsername() {
        assertThatThrownBy(() -> configuration(null, PASSWORD).pscdInboundUserDetailsService())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("refarch.pscd.inbound.security.username");
    }

    @Test
    void refusesToStartWithoutAPassword() {
        assertThatThrownBy(() -> configuration(USERNAME, null).pscdInboundUserDetailsService())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must both be set");
    }

    /** Blank counts as unset: an emptied value would otherwise build an account nobody can use. */
    @Test
    void refusesToStartOnBlankCredentials() {
        assertThatThrownBy(() -> configuration("   ", PASSWORD).pscdInboundUserDetailsService())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> configuration(USERNAME, "   ").pscdInboundUserDetailsService())
                .isInstanceOf(IllegalStateException.class);
        assertThatCode(() -> configuration(USERNAME, PASSWORD).pscdInboundUserDetailsService())
                .doesNotThrowAnyException();
    }

    private static PscdInboundSecurityConfiguration configuration(final String username, final String password) {
        final PscdInboundProperties properties = new PscdInboundProperties();
        properties.getSecurity().setUsername(username);
        properties.getSecurity().setPassword(password);
        return new PscdInboundSecurityConfiguration(properties);
    }
}
