package de.muenchen.oss.refarch.gateway.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.SuppressMatchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("refarch.security")
@Validated
public class SecurityProperties {
    /**
     * List of url patterns excluded from csrf protection.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", matchType = SuppressMatchType.EXACT)
    @NotNull private List<@NotBlank String> csrfWhitelisted = List.of();
}
