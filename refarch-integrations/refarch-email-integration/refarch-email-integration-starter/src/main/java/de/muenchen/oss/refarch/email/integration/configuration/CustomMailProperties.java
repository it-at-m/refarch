package de.muenchen.oss.refarch.email.integration.configuration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "refarch.mail")
@Validated
public class CustomMailProperties {
    /**
     * If connection to mail server should be tested on startup.
     */
    private boolean testConnection = true;

    /**
     * Sender mail address.
     */
    @NotBlank @Email private String fromAddress;

    /**
     * Default Reply-to mail address, e.g. no-reply@domain
     */
    @Email private String defaultReplyToAddress;

}
