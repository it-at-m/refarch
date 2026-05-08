package de.muenchen.oss.refarch.gateway.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("refarch.security")
@Validated
@SuppressFBWarnings("EI_EXPOSE_REP")
public class SecurityProperties {
    /**
     * List of url patterns excluded from csrf protection.
     */
    @NotNull private List<@NotBlank String> csrfWhitelisted = List.of();

    /**
     * Additional patterns that should be treated like client routes (JWT-authenticated, under
     * `/clients/**` by default).
     * Each pattern is an Ant-style glob that will be matched in the dedicated client security filter
     * chain.
     */
    @NotNull private List<@NotBlank String> clientPatterns = List.of();

    /**
     * List of rules for publicly accessible routes (permitAll, like `/public/**`), combining a path
     * pattern with specific HTTP methods.
     * All methods must be explicitly listed, there is no implicit "all".
     * WARNING: Security is DISABLED for matching requests.
     */
    @Valid @NotNull private List<@NotNull PermitRule> publicPatterns = List.of();

    /**
     * Rule describing a public endpoint combination consisting of a path pattern and the explicitly
     * allowed HTTP methods. Patterns use Spring's Ant-style path syntax (e.g. "/api/**").
     * All methods must be listed explicitly; there is no implicit "all methods" default.
     */
    @Data
    public static class PermitRule {
        /** Ant-style glob pattern, e.g. "/api/**". */
        @NotBlank private String pattern;

        /** List of HTTP methods like [GET, POST]. */
        @NotEmpty private List<@NotNull HttpMethod> methods;
    }
}
