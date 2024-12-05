package de.muenchen.refarch.gateway.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("refarch.security")
public class SecurityProperties {
    /**
     * List of url patterns excluded from csrf protection.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private List<String> csrfWhitelisted = List.of();
}
