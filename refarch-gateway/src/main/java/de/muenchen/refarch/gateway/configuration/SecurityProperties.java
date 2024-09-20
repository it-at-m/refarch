package de.muenchen.refarch.gateway.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("refarch.security")
public class SecurityProperties {
    /**
     * List of url patterns excluded from csrf protection.
     */
    private final List<String> csrfWhitelisted = List.of();

    public List<String> getCsrfWhitelisted() {
        return List.copyOf(this.csrfWhitelisted);
    }
}
