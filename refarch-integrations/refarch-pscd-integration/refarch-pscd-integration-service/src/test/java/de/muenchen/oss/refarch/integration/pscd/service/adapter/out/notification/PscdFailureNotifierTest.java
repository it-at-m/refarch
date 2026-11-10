package de.muenchen.oss.refarch.integration.pscd.service.adapter.out.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.email.domain.exception.SendMailException;
import de.muenchen.oss.refarch.integration.email.domain.model.TextMail;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdValidationException;
import de.muenchen.oss.refarch.integration.pscd.service.adapter.in.PscdInboundChannel;
import de.muenchen.oss.refarch.integration.pscd.service.configuration.PscdNotificationProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.DefaultResourceLoader;

class PscdFailureNotifierTest {

    private static final String RECIPIENTS = "ops@example.org,duty@example.org";

    /** The batch that failed, and a failure to report about it. */
    private static final String BATCH = "batch.txt";
    private static final String REASON = "nope";

    @TempDir
    private Path templates;

    private final MailOutPort mailOutPort = mock(MailOutPort.class);

    @Test
    void sendsTheRenderedTemplateToTheConfiguredRecipients() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.FILE);
        properties.setSubject("PSCD failed");
        properties.setTemplate(template("""
                channel=%1$s batch=%2$s location=%3$s
                reason=%5$s"""));

        notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, ".error/batch_20260804_161500.txt",
                new PscdValidationException("Rejected PSCD batch"));

        final TextMail sent = sentMail();
        assertThat(sent.getReceivers()).isEqualTo(RECIPIENTS);
        assertThat(sent.getSubject()).isEqualTo("PSCD failed");
        assertThat(sent.getBody()).isEqualTo("""
                channel=file batch=batch.txt location=.error/batch_20260804_161500.txt
                reason=PscdValidationException: Rejected PSCD batch""");
    }

    /** The timestamp is the one argument the caller does not supply, so it is only checked in shape. */
    @Test
    void stampsTheTimeTheFailureWasReported() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.REST);
        properties.setTemplate(template("%4$s"));

        notifier(properties).notifyFailure(PscdInboundChannel.REST, BATCH, null, new IllegalStateException(REASON));

        assertThat(sentMail().getBody()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void reportsTheChannelsOwnNameAndAPlaceholderWhereThereIsNoLocation() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.SOAP);
        properties.setTemplate(template("%1$s|%3$s"));

        notifier(properties).notifyFailure(PscdInboundChannel.SOAP, BATCH, null, new IllegalStateException(REASON));

        assertThat(sentMail().getBody()).isEqualTo("SOAP|-");
    }

    /** A filename off the SOAP or REST payload is untrusted, so it must not forge lines in the mail. */
    @Test
    void sanitizesTheFilenameItPutsInTheMail() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.REST);
        properties.setTemplate(template("%2$s"));

        notifier(properties).notifyFailure(PscdInboundChannel.REST, "evil\nReason: all fine", null,
                new IllegalStateException(REASON));

        assertThat(sentMail().getBody()).isEqualTo("evil?Reason: all fine");
    }

    @Test
    void reportsAnExceptionWithoutAMessageByItsTypeAlone() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.FILE);
        properties.setTemplate(template("%5$s"));

        notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, null, new IllegalStateException());

        assertThat(sentMail().getBody()).isEqualTo("IllegalStateException");
    }

    @Test
    void staysSilentWhileTheChannelIsNotEnabled() {
        final PscdNotificationProperties properties = new PscdNotificationProperties();
        properties.setTo(RECIPIENTS);

        notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, null, new IllegalStateException(REASON));

        verifyNoInteractions(this.mailOutPort);
    }

    /** Each channel switches only itself: enabling the file channel must not mail REST failures. */
    @Test
    void notifiesOnlyForTheChannelThatIsEnabled() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.FILE);
        properties.setTemplate(template("%1$s"));
        final PscdFailureNotifier notifier = notifier(properties);

        notifier.notifyFailure(PscdInboundChannel.REST, BATCH, null, new IllegalStateException(REASON));
        notifier.notifyFailure(PscdInboundChannel.SOAP, BATCH, null, new IllegalStateException(REASON));
        verifyNoInteractions(this.mailOutPort);

        notifier.notifyFailure(PscdInboundChannel.FILE, BATCH, null, new IllegalStateException(REASON));
        assertThat(sentMail().getBody()).isEqualTo("file");
    }

    /** An enabled channel without a recipient is a misconfiguration, not something to fail on. */
    @Test
    void staysSilentWithoutARecipient() {
        final PscdNotificationProperties properties = new PscdNotificationProperties();
        properties.getFile().setEnabled(true);

        notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, null, new IllegalStateException(REASON));

        verifyNoInteractions(this.mailOutPort);
    }

    /** The alert still has to go out when its template does not, with the values in it. */
    @Test
    void fallsBackToThePlainValuesWhenTheTemplateCannotBeRead() {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.FILE);
        properties.setTemplate(this.templates.resolve("absent.txt").toUri().toString());

        notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, ".error/batch_20260804_161500.txt",
                new PscdValidationException("Rejected PSCD batch"));

        assertThat(sentMail().getBody())
                .contains("Channel:  file")
                .contains("Batch:    batch.txt")
                .contains("Location: .error/batch_20260804_161500.txt")
                .contains("Reason:   PscdValidationException: Rejected PSCD batch");
    }

    /** A stray percent sign in an operator-maintained template must not silence the alert either. */
    @Test
    void fallsBackToThePlainValuesWhenTheTemplateIsNotAValidFormat() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.FILE);
        properties.setTemplate(template("100% of the batch was rejected: %1$s"));

        notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, null, new IllegalStateException(REASON));

        assertThat(sentMail().getBody()).contains("Channel:  file");
    }

    /** Notification runs on a path that is already failing; it may never add a failure of its own. */
    @Test
    void swallowsAFailureToSend() throws IOException {
        final PscdNotificationProperties properties = enabledFor(PscdInboundChannel.FILE);
        properties.setTemplate(template("%1$s"));
        doThrow(new SendMailException("smtp unreachable", new RuntimeException())).when(this.mailOutPort).sendTextMail(any());

        assertThatCode(() -> notifier(properties).notifyFailure(PscdInboundChannel.FILE, BATCH, null,
                new IllegalStateException(REASON))).doesNotThrowAnyException();
    }

    private PscdFailureNotifier notifier(final PscdNotificationProperties properties) {
        return new PscdFailureNotifier(this.mailOutPort, properties, new DefaultResourceLoader());
    }

    private PscdNotificationProperties enabledFor(final PscdInboundChannel channel) {
        final PscdNotificationProperties properties = new PscdNotificationProperties();
        properties.setTo(RECIPIENTS);
        switch (channel) {
        case FILE -> properties.getFile().setEnabled(true);
        case REST -> properties.getRest().setEnabled(true);
        case SOAP -> properties.getSoap().setEnabled(true);
        }
        return properties;
    }

    /**
     * Write a body template and return its location, as an operator's {@code file:} template would be.
     */
    private String template(final String content) throws IOException {
        final Path file = this.templates.resolve("failure.txt");
        Files.writeString(file, content);
        return file.toUri().toString();
    }

    private TextMail sentMail() {
        final ArgumentCaptor<TextMail> captor = ArgumentCaptor.forClass(TextMail.class);
        verify(this.mailOutPort).sendTextMail(captor.capture());
        return captor.getValue();
    }
}
