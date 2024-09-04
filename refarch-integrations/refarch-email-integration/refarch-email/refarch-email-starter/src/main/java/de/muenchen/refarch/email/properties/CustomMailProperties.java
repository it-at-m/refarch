package de.muenchen.refarch.email.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "refarch.mail")
public class CustomMailProperties {

    /**
     * Sender mail address.
     */
    private String fromAddress;

    /**
     * Default Reply-to mail address, e.g. no-reply@domain
     */
    private String defaultReplyToAddress;

}
