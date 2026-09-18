package de.muenchen.oss.refarch.integration.pscd.service;

import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.ACCOUNT_ERROR_LOG;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.COMPLETION_LOG;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.addedTo;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.archived;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.deliver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.email.domain.model.TextMail;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.Delivery;
import jakarta.xml.ws.WebServiceException;
import java.net.ConnectException;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * What happens to a perfectly good batch when PSCD is not there: the case the sample
 * {@code test_service_not_available} is kept for.
 *
 * <p>
 * The distinction this pins down is between a batch that is wrong and a batch that could not be
 * delivered. The file here is sound: one control record, every mandatory field present. So nothing
 * goes into the error trail and no record-error mail is due; what is due is the <em>failure</em>
 * mail, the file in the error archive rather than the successful one, and no completion line,
 * because nothing reached PSCD.
 * </p>
 *
 * <p>
 * The endpoint is made unavailable at the SOAP client, which is where a refused connection surfaces
 * in
 * production: CXF throws from {@code send}, {@code PscdOutAdapter} wraps it as a
 * {@code PscdProcessingException}, and the poller takes it from there.
 * </p>
 */
@PscdFileChannelTest
class PscdServiceEndpointUnavailableTest {

    /** The sample, unmodified: a lone control record, enough to be a valid batch. */
    private static final String BATCH = "test_service_not_available";

    // Static so @DynamicPropertySource can expose it before the context loads; @TempDir requires it mutable.
    @SuppressWarnings("PMD.MutableStaticState")
    @TempDir
    /* default */ static Path inbox;

    /**
     * Stands in for an endpoint that is down: every send fails the way CXF fails on a refused socket.
     */
    @MockitoBean
    private PscdSoapClient pscdSoapClient;

    @MockitoBean
    private MailOutPort mailOutPort;

    @DynamicPropertySource
    /* default */ static void inboxProperty(final DynamicPropertyRegistry registry) {
        registry.add("refarch.pscd.inbound.file.directory", inbox::toString);
    }

    @Test
    void aBatchThatCouldNotBeDeliveredIsDivertedAndMailedAbout() throws Exception {
        doThrow(new WebServiceException("Could not send Message.", new ConnectException("Connection refused")))
                .when(this.pscdSoapClient).send(any());

        // The send is attempted, and throws. Everything below is what the service makes of that.
        final Delivery delivery = deliver(inbox, BATCH, this.pscdSoapClient);
        assertThat(delivery.request().getSatzart010()).isNotNull();

        // --- the file goes to the error archive ----------------------------------------------------
        await().atMost(Duration.ofSeconds(15)).until(() -> archived(inbox, "archive/error", BATCH));
        assertThat(archived(inbox, "archive/successful", BATCH))
                .as("a batch PSCD never received must not be filed as successful")
                .isFalse();
        assertThat(inbox.resolve(BATCH))
                .as("and it must not stay in the inbox to be picked up again")
                .doesNotExist();

        // --- the failure mail ----------------------------------------------------------------------
        final ArgumentCaptor<TextMail> mail = ArgumentCaptor.forClass(TextMail.class);
        verify(this.mailOutPort, timeout(15_000)).sendTextMail(mail.capture());
        final TextMail sent = mail.getValue();
        assertThat(sent.getReceivers()).isEqualTo("doesnotexist@muenchen.de");
        // The failure subject, not the record-error one: the batch was not delivered at all.
        assertThat(sent.getSubject()).isEqualTo("PSCD batch processing failed");
        assertThat(sent.getBody())
                .contains("Channel:  file")
                .contains("Batch:    " + BATCH)
                // Where to find the batch now, so it can be put back once PSCD is up.
                .containsPattern("Location: archive/error/" + BATCH + "_\\d{8}_\\d{9}")
                .contains("PscdProcessingException: Failed to call the PSCD SOAP endpoint");

        // --- the accounting trails -----------------------------------------------------------------
        assertThat(addedTo(ACCOUNT_ERROR_LOG, BATCH, delivery.accountErrorsBefore()))
                .as("nothing was wrong with the records; the endpoint was the problem")
                .isEmpty();
        assertThat(addedTo(COMPLETION_LOG, BATCH, delivery.completionsBefore()))
                .as("the completion trail is what PSCD received, so an undelivered batch is absent from it")
                .isEmpty();
    }
}
