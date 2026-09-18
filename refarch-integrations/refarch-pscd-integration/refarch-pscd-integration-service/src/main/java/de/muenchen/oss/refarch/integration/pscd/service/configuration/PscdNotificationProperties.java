package de.muenchen.oss.refarch.integration.pscd.service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Failure-notification configuration for the standalone service
 * ({@code refarch.pscd.notification}).
 *
 * <p>
 * The mail itself is sent through this repository's email integration, so the SMTP server
 * ({@code spring.mail.host}, {@code spring.mail.port}, …) and the sender address
 * ({@code refarch.mail.from-address}) are configured there, not here. What is left is who is told,
 * what the subject says, and where the body text comes from.
 * </p>
 *
 * <p>
 * Off by default on every channel and turning this on is a deliberate act. The three
 * toggles mirror {@link PscdInboundProperties}: each inbound channel is notified independently,
 * because their failures reach very different audiences (a file failure is unattended, while a
 * rejected SOAP or REST payload is also reported to the caller).
 * </p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "refarch.pscd.notification")
public class PscdNotificationProperties {

    /** Recipients of the failure mail, comma separated. Nothing is sent while this is blank. */
    private String to;

    private String subject = "PSCD batch processing failed";

    /**
     * Subject line of the record-error mail: the batch <em>was</em> delivered, but some of its records
     * could not be processed as they arrived. Kept apart from {@link #subject} because the two mails
     * need different reactions.
     */
    private String recordErrorSubject = "PSCD batch delivered with record errors";

    /**
     * Spring resource location of the mail body template: {@code classpath:} for a template shipped
     * with the service, {@code file:} for one an operator maintains next to the deployment.
     *
     * <p>
     * The template is a plain-text {@code String.format} pattern. Its arguments are always passed in
     * this order, so a placeholder may be repeated or skipped but not reordered without
     * {@code %n$s} indexes:
     * </p>
     *
     * <ol>
     * <li>{@code %1$s}: inbound channel the batch arrived on ({@code file}, {@code REST},
     * {@code SOAP})</li>
     * <li>{@code %2$s}: batch filename, or {@code <none>} if the payload carried none</li>
     * <li>{@code %3$s}: where the batch is now (the file channel's error directory entry), or
     * {@code -} on the channels that do not keep the payload</li>
     * <li>{@code %4$s}: when it failed, as {@code yyyy-MM-dd HH:mm:ss}</li>
     * <li>{@code %5$s}: the failure itself, exception type and message</li>
     * </ol>
     *
     * <p>
     * A literal percent sign therefore has to be written {@code %%}. If the template cannot be read or
     * cannot be formatted, the mail is still sent, with a plain listing of the five values as its
     * body, since an alert that arrives in a worse shape beats no alert.
     * </p>
     */
    private String template = "classpath:mail/pscd-failure.txt";

    // refarch.pscd.notification.{file,rest,soap}.enabled
    @NestedConfigurationProperty
    private ChannelProperties file = new ChannelProperties();

    @NestedConfigurationProperty
    private ChannelProperties rest = new ChannelProperties();

    @NestedConfigurationProperty
    private ChannelProperties soap = new ChannelProperties();

    @Getter
    @Setter
    public static class ChannelProperties {
        private boolean enabled;
    }
}
