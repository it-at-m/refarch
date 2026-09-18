package de.muenchen.oss.refarch.integration.pscd.service;

import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.ACCOUNT_ERROR_LOG;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.COMPLETION_LOG;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.addedTo;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.archived;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.deliver;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.linesFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzartFehler;
import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.email.domain.model.TextMail;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.Delivery;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The legacy end-to-end test, brought over: the predecessor application had a test around this very
 * file, {@code test_satzarten_not_found}, and it is the reason the file is kept in this repository.
 * The point of it there and here is the same: a batch that contains record types PSCD does not know
 * must still be delivered, with the bad lines reported rather than the good ones lost.
 *
 * <p>
 * The file is read as it is and never modified: it is evidence of the format as the predecessor
 * produced it, so the test copies it into the polled directory and asserts against what the parser
 * makes of it.
 * </p>
 *
 * <p>
 * What it covers, in one pass through the whole file channel: the request that reaches the PSCD
 * SOAP client, the archive the file is moved to, both accounting trails, and the mail feature.
 * The mail port is a mock, so no message can leave the JVM whatever the notification settings say.
 * </p>
 */
@PscdFileChannelTest
class PscdServiceLegacySatzartenNotFoundTest {

    /** The legacy sample, unmodified: 4× SATZART 100, 2× 200, 1× 010 and the codes 600, 610, 999. */
    private static final String BATCH = "test_satzarten_not_found";

    // Static so @DynamicPropertySource can expose it before the context loads; @TempDir requires it mutable.
    @SuppressWarnings("PMD.MutableStaticState")
    @TempDir
    /* default */ static Path inbox;

    /**
     * The SOAP client is the last thing before the wire, so mocking it captures exactly the request
     * PSCD would receive, and guarantees nothing is actually sent.
     */
    @MockitoBean
    private PscdSoapClient pscdSoapClient;

    /** Never a real mail: the port that would reach the SMTP server is a mock. */
    @MockitoBean
    private MailOutPort mailOutPort;

    @DynamicPropertySource
    /* default */ static void inboxProperty(final DynamicPropertyRegistry registry) {
        registry.add("refarch.pscd.inbound.file.directory", inbox::toString);
    }

    @Test
    void legacyBatchWithUnknownSatzartCodesIsDeliveredArchivedAndAccountedFor() throws Exception {
        // --- the request that reaches PSCD ---------------------------------------------------------
        final Delivery delivery = deliver(inbox, BATCH, this.pscdSoapClient);
        final DTSOAPSatzarten request = delivery.request();

        assertThat(request.getFilename()).isEqualTo(BATCH);
        assertThat(request.getSatzart010()).isNotNull();
        assertThat(request.getSatzart100()).hasSize(4);
        assertThat(request.getSatzart200()).hasSize(2);
        // The three lines whose SATZART is not a known record type travel as error records, the
        // vendor contract's own way of reporting a record that could not be processed.
        assertThat(request.getSatzartFehler())
                .extracting(DTSatzartFehler::getSATZART)
                .containsExactly("600", "610", "999");
        assertThat(request.getSatzartFehler())
                .allSatisfy(fehler -> assertThat(fehler.getFEHLERTEXT()).contains("unknown SATZART code"));

        // --- the file is archived, not diverted ----------------------------------------------------
        await().atMost(Duration.ofSeconds(15)).until(() -> archived(inbox, "archive/successful", BATCH));
        assertThat(archived(inbox, "archive/error", BATCH)).isFalse();
        assertThat(inbox.resolve(BATCH)).doesNotExist();

        // --- logs/account-error.log ----------------------------------------------------------------
        await().atMost(Duration.ofSeconds(15))
                .until(() -> linesFor(ACCOUNT_ERROR_LOG, BATCH).size() > delivery.accountErrorsBefore());
        final List<String> accountErrors = addedTo(ACCOUNT_ERROR_LOG, BATCH, delivery.accountErrorsBefore());
        assertThat(accountErrors)
                .as("the five records this file could not deliver as they arrived, as the legacy test counted them")
                .hasSize(5);
        // Three lines name a record type PSCD does not have.
        assertThat(accountErrors.stream().filter(line -> line.contains("unknown SATZART code"))).hasSize(3);
        assertThat(accountErrors)
                .anySatisfy(line -> assertThat(line).contains("line 2").contains("'600'"))
                .anySatisfy(line -> assertThat(line).contains("line 5").contains("'610'"))
                .anySatisfy(line -> assertThat(line).contains("line 8").contains("'999'"));
        // The two SATZART 200 records leave FV_BELNR empty: columns 412-423 are within the line, they
        // simply carry spaces, so the field was filled in and the record reported.
        assertThat(accountErrors.stream().filter(line -> line.contains("missing FV_BELNR"))).hasSize(2);
        assertThat(accountErrors)
                .anySatisfy(line -> assertThat(line).contains("line 7").contains("SATZART 200"))
                .anySatisfy(line -> assertThat(line).contains("line 9").contains("SATZART 200"));
        // The four SATZART 100 lines are NOT in here: they stop at 426 characters where the transcribed
        // layout reaches 821, but everything past 426 is optional, so nothing was lost. Their records
        // still say TRUNCATED: being short is reported, it is just not an accounting error.
        assertThat(accountErrors.stream().filter(line -> line.contains("SATZART 100"))).isEmpty();

        // --- logs/completion.log -------------------------------------------------------------------
        final List<String> completions = addedTo(COMPLETION_LOG, BATCH, delivery.completionsBefore());
        assertThat(completions)
                .as("one line per processed file, whatever the batch held")
                .singleElement()
                // Ten of the file's ten lines went to PSCD: 1×010 + 4×100 + 2×200 and the 3 error
                // records standing in for the lines whose SATZART was unknown.
                .satisfies(line -> assertThat(line)
                        .contains("10 of 10 entries from the file '" + BATCH + "' sent to PSCD"));

        // --- the mail feature ----------------------------------------------------------------------
        // The batch went through, so this is not a failure mail. It is the record-error mail, carrying
        // the same five entries the accounting trail holds. One mail per batch, not one per record.
        final ArgumentCaptor<TextMail> mail = ArgumentCaptor.forClass(TextMail.class);
        verify(this.mailOutPort, timeout(15_000)).sendTextMail(mail.capture());
        final TextMail sent = mail.getValue();
        assertThat(sent.getReceivers()).isEqualTo("doesnotexist@muenchen.de");
        assertThat(sent.getSubject()).isEqualTo("PSCD batch delivered with record errors");
        assertThat(sent.getBody())
                .contains("Channel:  file")
                .contains("Batch:    " + BATCH)
                // Where the file was archived, so the mail says where to look.
                .containsPattern("Location: archive/successful/" + BATCH + "_\\d{8}_\\d{9}")
                .contains("5 record(s) could not be processed as they arrived")
                .contains("unknown SATZART code '600'")
                .contains("unknown SATZART code '610'")
                .contains("unknown SATZART code '999'")
                .contains("line 7: SATZART 200 is missing FV_BELNR")
                .contains("line 9: SATZART 200 is missing FV_BELNR");
    }

}
