package de.muenchen.oss.refarch.integration.pscd.service.adapter.out.notification;

import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.email.domain.model.TextMail;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdBatchLog;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundChannel;
import de.muenchen.oss.refarch.integration.pscd.service.configuration.PscdNotificationProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.IllegalFormatException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Tells the configured recipients that a PSCD batch could not be processed.
 *
 * <p>
 * A failure is already logged by the adapter that hit it; this adds the push, because the two
 * unattended cases (a diverted file and a batch that could not be delivered onwards) are otherwise
 * only visible to whoever reads the log. Which channels notify, who is told, and the body text are
 * configured under {@code refarch.pscd.notification} ({@link PscdNotificationProperties}); the mail
 * itself goes out through this repository's email integration, which owns the SMTP settings.
 * </p>
 *
 * <p>
 * <strong>Never throws.</strong> Notification is a side channel: it runs on a path that is already
 * failing, and an unreachable SMTP server must not turn a diverted batch into a batch that was
 * never
 * moved, nor change the fault a caller receives. Everything that goes wrong in here is logged and
 * swallowed.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PscdFailureNotifier {

    private static final DateTimeFormatter FAILED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String NO_LOCATION = "-";

    private static final int MAX_REPORTED_PROBLEMS = 50;

    private final MailOutPort mailOutPort;
    private final PscdNotificationProperties properties;
    private final ResourceLoader resourceLoader;

    /**
     * Send the failure mail for one batch, if the channel it arrived on is configured to notify.
     *
     * @param channel the inbound channel the batch arrived on
     * @param filename the batch filename as it arrived, untrusted on the SOAP and REST channels, so it
     *            is sanitized before it goes into the mail
     * @param location where the batch can be found now, or {@code null} on the channels that do not
     *            keep it
     * @param cause what went wrong
     */
    public void notifyFailure(final PscdInboundChannel channel, final String filename, final String location, final Throwable cause) {
        send(channel, filename, location, describe(cause), this.properties.getSubject());
    }

    /**
     * Send the record-error mail for a batch that <em>was</em> delivered but carried records PSCD could
     * not be given as they arrived: an unknown SATZART, or a mandatory field that had to be filled in.
     *
     * <p>
     * One mail per batch, not per record: the body lists what
     * {@code logs/account-error.log} recorded, bounded at {@value #MAX_REPORTED_PROBLEMS}
     * entries so a
     * thoroughly broken file cannot produce an unreadable mail. Same recipients, same template and the
     * same per-channel toggle as a failure; only the subject differs, because nothing failed here and
     * whoever triages the mail should see that at a glance.
     * </p>
     *
     * @param channel the inbound channel the batch arrived on
     * @param filename the batch filename as it arrived
     * @param location where the batch can be found now: its archive, since it was processed
     * @param problems what could not be processed, one entry per record
     */
    public void notifyRecordErrors(final PscdInboundChannel channel, final String filename, final String location,
            final List<String> problems) {
        if (problems.isEmpty()) {
            return;
        }
        send(channel, filename, location, summarise(problems), this.properties.getRecordErrorSubject());
    }

    /**
     * The one path to the mail port: nothing is sent while the channel is switched off or no recipient
     * is configured, and a send that fails is logged rather than thrown.
     */
    private void send(final PscdInboundChannel channel, final String filename, final String location,
            final String reason, final String subject) {
        if (!enabledFor(channel)) {
            return;
        }
        final String recipients = this.properties.getTo();
        if (recipients == null || recipients.isBlank()) {
            // Configuring a channel to notify but no recipient is a misconfiguration, not a reason to
            // fail: warn where it will be noticed and carry on.
            log.warn("PSCD notification is enabled for the {} channel but refarch.pscd.notification.to is not set; "
                    + "no mail sent for batch '{}'", channel.label(), PscdBatchLog.safeFilename(filename));
            return;
        }
        try {
            this.mailOutPort.sendTextMail(new TextMail(recipients, null, null, subject,
                    body(channel, filename, location, reason), null, null));
            log.debug("Sent PSCD notification for the {} channel to '{}'", channel.label(), recipients);
        } catch (final RuntimeException e) {
            log.error("Could not send the PSCD notification for batch '{}' to '{}'",
                    PscdBatchLog.safeFilename(filename), recipients, e);
        }
    }

    /** The record errors as one reason line, with an exact count and a bound on what is spelled out. */
    private static String summarise(final List<String> problems) {
        final List<String> reported = problems.size() > MAX_REPORTED_PROBLEMS
                ? problems.subList(0, MAX_REPORTED_PROBLEMS)
                : problems;
        final String omitted = problems.size() > MAX_REPORTED_PROBLEMS
                ? " … and %d more".formatted(problems.size() - MAX_REPORTED_PROBLEMS)
                : "";
        return "%d record(s) could not be processed as they arrived: %s%s"
                .formatted(problems.size(), String.join("; ", reported), omitted);
    }

    private boolean enabledFor(final PscdInboundChannel channel) {
        return switch (channel) {
        case FILE -> this.properties.getFile().isEnabled();
        case REST -> this.properties.getRest().isEnabled();
        case SOAP -> this.properties.getSoap().isEnabled();
        };
    }

    /**
     * Render the configured template with this failure's values. A template that cannot be read or
     * cannot be formatted falls back to a plain listing of the same values, so the alert still goes
     * out; see {@link PscdNotificationProperties#getTemplate()} for the argument order.
     */
    private String body(final PscdInboundChannel channel, final String filename, final String location, final String reason) {
        final Object[] values = {
                channel.label(),
                PscdBatchLog.safeFilename(filename),
                location == null || location.isBlank() ? NO_LOCATION : location,
                LocalDateTime.now().format(FAILED_AT),
                reason
        };
        final String template = template();
        if (template == null) {
            return fallbackBody(values);
        }
        try {
            return template.formatted(values);
        } catch (final IllegalFormatException e) {
            log.error("PSCD failure notification template '{}' is not a valid format string; sending the plain values instead",
                    this.properties.getTemplate(), e);
            return fallbackBody(values);
        }
    }

    /** The template's text, or {@code null} if it cannot be read. */
    private String template() {
        final Resource resource = this.resourceLoader.getResource(this.properties.getTemplate());
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (final IOException e) {
            log.error("Could not read the PSCD failure notification template '{}'; sending the plain values instead",
                    this.properties.getTemplate(), e);
            return null;
        }
    }

    /** What a mail says when its template is unusable: the same values, one per line. */
    private static String fallbackBody(final Object... values) {
        return """
                PSCD batch processing failed.

                Channel:  %1$s
                Batch:    %2$s
                Location: %3$s
                Failed:   %4$s
                Reason:   %5$s
                """.formatted(values);
    }

    /**
     * The failure as one line: the exception's type and message, or just its type if it carries none.
     */
    private static String describe(final Throwable cause) {
        if (cause == null) {
            return "unknown";
        }
        final String message = cause.getMessage();
        return message == null || message.isBlank()
                ? cause.getClass().getSimpleName()
                : cause.getClass().getSimpleName() + ": " + message;
    }
}
