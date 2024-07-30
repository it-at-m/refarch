package de.muenchen.refarch.gateway.configuration;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("refarch.security")
public class SecurityProperties {
    /**
     * List of url patterns excluded from csrf protection.
     */
    private List<String> csrfWhitelisted = List.of();
}
