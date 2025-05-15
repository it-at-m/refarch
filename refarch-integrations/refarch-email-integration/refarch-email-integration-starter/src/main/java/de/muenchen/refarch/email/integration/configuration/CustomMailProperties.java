package de.muenchen.refarch.email.integration.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "refarch.mail")
public class CustomMailProperties {
    /**
     * If connection to mail server should be tested on startup.
     */
    private boolean testConnection = true;

    /**
     * Sender mail address.
     */
    private String fromAddress;

    /**
     * Default Reply-to mail address, e.g. no-reply@domain
     */
    private String defaultReplyToAddress;

}
