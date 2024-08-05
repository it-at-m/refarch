package de.muenchen.refarch.spring.security.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refarch.security")
@Data
public class SpringSecurityProperties {
    /**
     * Name of the registration used in main security protection (as resource server).
     */
    private String clientRegistration = "keycloak";

    private String clientRegistrationServiceAccount = "keycloak-service-account";

    /**
     * Client id to use.
     */
    private String clientId;

    /**
     * Client secret to use.
     */
    private String clientSecret;

    private String[] permittedUrls = {
            "/error", // allow the error page
            "/actuator/info", // allow access to /actuator/info
            "/actuator/health", // allow access to /actuator/health for OpenShift Health Check
            "/actuator/metrics", // allow access to /actuator/metrics for Prometheus monitoring in OpenShift
    };

    /**
     * Username fallback to use in no-security mode.
     */
    private String fallbackUsername = "externer.john.doe";

    /**
     * Roles to fallback to use in no-security mode.
     */
    private String[] fallbackUserRoles = { "office", "administration" };
}
