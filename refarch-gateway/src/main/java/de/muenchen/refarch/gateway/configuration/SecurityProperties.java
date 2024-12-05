package de.muenchen.refarch.gateway.configuration;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("refarch.security")
public class SecurityProperties {
    /**
     * List of url patterns excluded from csrf protection.
     */
    private List<String> csrfWhitelisted = List.of();

    public List<String> getCsrfWhitelisted() {
        return List.copyOf(this.csrfWhitelisted);
    }
}
