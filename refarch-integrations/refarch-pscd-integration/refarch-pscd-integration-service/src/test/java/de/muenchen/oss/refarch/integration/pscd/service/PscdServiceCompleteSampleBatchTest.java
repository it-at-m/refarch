package de.muenchen.oss.refarch.integration.pscd.service;

import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.ACCOUNT_ERROR_LOG;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.COMPLETION_LOG;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.addedTo;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.archived;
import static de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.deliver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verifyNoInteractions;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.Delivery;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The one sample covering every record type this integration maps: 902 records of nine SATZART
 * codes, 2583 characters per line, as the predecessor produced them.
 *
 * <p>
 * This is the broadest evidence there is that the column table works against real data: a batch
 * this size passes through with nothing filled in and nothing reported, so every mandatory column
 * of every record type is where the layout says it is. It is also the volume case, since the counts
 * below are what a change to the parser would move if it started dropping or duplicating records.
 * </p>
 *
 * <p>
 * The file is read exactly as it is.
 * </p>
 */
@PscdFileChannelTest
class PscdServiceCompleteSampleBatchTest {

    /** The sample, unmodified. Its records are counted below, per type. */
    private static final String BATCH = "d_gws_01_fwpkfbp0_20190329_w01_buchungssaetze";

    // Static so @DynamicPropertySource can expose it before the context loads; @TempDir requires it mutable.
    @SuppressWarnings("PMD.MutableStaticState")
    @TempDir
    /* default */ static Path inbox;

    /** The last thing before the wire: mocking it captures the request and sends nothing. */
    @MockitoBean
    private PscdSoapClient pscdSoapClient;

    /** Never a real mail, and this batch should not produce one at all. */
    @MockitoBean
    private MailOutPort mailOutPort;

    @DynamicPropertySource
    /* default */ static void inboxProperty(final DynamicPropertyRegistry registry) {
        registry.add("refarch.pscd.inbound.file.directory", inbox::toString);
    }

    @Test
    void everyRecordTypeOfTheCompleteSampleReachesPscdWithNothingReported() throws Exception {
        final Delivery delivery = deliver(inbox, BATCH, this.pscdSoapClient);
        final DTSOAPSatzarten request = delivery.request();

        assertThat(request.getFilename()).isEqualTo(BATCH);
        assertThat(request.getSatzart010().getABSTIMMSUMME()).isEqualTo("2648524762");
        assertThat(request.getSatzart010().getVORZEICHEN()).isEqualTo("+");
        assertThat(request.getSatzart100()).hasSize(47);
        assertThat(request.getSatzart105()).hasSize(11);
        assertThat(request.getSatzart155()).hasSize(1);
        assertThat(request.getSatzart165()).hasSize(34);
        assertThat(request.getSatzart200()).hasSize(474);
        assertThat(request.getSatzart210()).hasSize(26);
        assertThat(request.getSatzart250()).hasSize(304);
        assertThat(request.getSatzart260()).hasSize(4);
        assertThat(request.getSatzartFehler()).isEmpty();

        // --- archived as processed -----------------------------------------------------------------
        await().atMost(Duration.ofSeconds(15)).until(() -> archived(inbox, "archive/successful", BATCH));
        assertThat(archived(inbox, "archive/error", BATCH)).isFalse();

        // --- the accounting trails -----------------------------------------------------------------
        assertThat(addedTo(ACCOUNT_ERROR_LOG, BATCH, delivery.accountErrorsBefore()))
                .as("902 records of every type, and not one of them had to be repaired")
                .isEmpty();

        final List<String> completions = addedTo(COMPLETION_LOG, BATCH, delivery.completionsBefore());
        assertThat(completions).singleElement().satisfies(line -> assertThat(line)
                // Every one of the file's 902 lines became a record: 1 + 47 + 11 + 1 + 34 + 474 + 26 +
                // 304 + 4. Nothing was skipped, and nothing was replaced.
                .contains("902 of 902 entries from the file '" + BATCH + "' sent to PSCD"));

        // --- no mail -------------------------------------------------------------------------------
        verifyNoInteractions(this.mailOutPort);
    }
}
