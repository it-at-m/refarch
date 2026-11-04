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
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart210;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart260;
import de.muenchen.oss.refarch.integration.email.application.port.out.MailOutPort;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import de.muenchen.oss.refarch.integration.pscd.service.PscdFileChannelTestSupport.Delivery;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The two lengths SATZART 210 and 260 arrive in, each from a sample the predecessor produced: 445
 * characters with the trailing KOSTL / AUFNR / MWSKZ block, and 419 characters without it. Both are
 * complete records, since the vendor WSDL declares that block {@code minOccurs="0"}, so this is
 * also where a clean run is pinned down: nothing in the error trail, no mail, one completion line.
 *
 * <p>
 * The trailing block is what makes both worth testing: those three columns are the only fields on
 * this
 * channel a record may legitimately be without, so wrong offsets would not fail anywhere: they
 * would shift values into each other in the long variant, or invent values in the short one.
 * Asserting them present in one file and absent in the other pins the columns against real data.
 * </p>
 *
 * <p>
 * Both files are read exactly as they are.
 * </p>
 */
@PscdFileChannelTest
class PscdServiceSatzart210And260VariantsTest {

    /** The long variant: three SATZART 210 records and one 260, all 445 characters. */
    private static final String LONG_BATCH = "210_260_mit_KOSTL_AUFNR_MWKZ";

    /**
     * The short variant: three 260 records and three 210s, all 419 characters, the length the layout
     * requires, with nothing beyond it. Its name says 420; the lines are 419, which is what the parser
     * and the vendor WSDL agree on.
     */
    private static final String SHORT_BATCH = "d_gws_01_fwpkfbp0_20190329_w01_buchungssaetze_210_260_Length420Chars";

    /** The trailing block every record of the long variant carries, as it stands in the file. */
    private static final String KOSTL = "9999999999";
    private static final String AUFNR = "000999000010";
    private static final String MWSKZ = "A0";

    // Static so @DynamicPropertySource can expose it before the context loads; @TempDir requires it mutable.
    @SuppressWarnings("PMD.MutableStaticState")
    @TempDir
    /* default */ static Path inbox;

    /** The last thing before the wire: mocking it captures the request and sends nothing. */
    @MockitoBean
    private PscdSoapClient pscdSoapClient;

    /** Never a real mail, and neither of these batches should produce one at all. */
    @MockitoBean
    private MailOutPort mailOutPort;

    @DynamicPropertySource
    /* default */ static void inboxProperty(final DynamicPropertyRegistry registry) {
        registry.add("refarch.pscd.inbound.file.directory", inbox::toString);
    }

    @Test
    void longVariantCarriesTheTrailingBlockToPscd() throws Exception {
        final Delivery delivery = deliver(inbox, LONG_BATCH, this.pscdSoapClient);
        final DTSOAPSatzarten request = delivery.request();

        assertThat(request.getFilename()).isEqualTo(LONG_BATCH);
        assertThat(request.getSatzart210()).hasSize(3);
        assertThat(request.getSatzart260()).hasSize(1);

        final DTSatzart210 firstSatzart210 = request.getSatzart210().get(0);
        assertThat(firstSatzart210.getKOSTL()).isEqualTo(KOSTL);
        assertThat(firstSatzart210.getAUFNR()).isEqualTo(AUFNR);
        assertThat(firstSatzart210.getMWSKZ()).isEqualTo(MWSKZ);

        final DTSatzart260 firstSatzart260 = request.getSatzart260().get(0);
        assertThat(firstSatzart260.getKOSTL()).isEqualTo(KOSTL);
        assertThat(firstSatzart260.getAUFNR()).isEqualTo(AUFNR);
        assertThat(firstSatzart260.getMWSKZ()).isEqualTo(MWSKZ);

        // Four entries, not five: this file has no control record, which is not an error.
        assertThat(request.getSatzart010()).isNull();
        assertNothingReported(delivery, LONG_BATCH, "4 of 4");
    }

    @Test
    void shortVariantWithoutTheTrailingBlockIsDeliveredJustAsCleanly() throws Exception {
        final Delivery delivery = deliver(inbox, SHORT_BATCH, this.pscdSoapClient);
        final DTSOAPSatzarten request = delivery.request();

        assertThat(request.getFilename()).isEqualTo(SHORT_BATCH);
        assertThat(request.getSatzart210()).hasSize(3);
        assertThat(request.getSatzart260()).hasSize(3);

        // The record ends after FV_BELNR at column 419, so the trailing block is simply not there:
        // unset rather than filled in, because those three columns are optional.
        final DTSatzart210 firstSatzart210 = request.getSatzart210().get(0);
        assertThat(firstSatzart210.getKOSTL()).isNull();
        assertThat(firstSatzart210.getAUFNR()).isNull();
        assertThat(firstSatzart210.getMWSKZ()).isNull();

        final DTSatzart260 firstSatzart260 = request.getSatzart260().get(0);
        assertThat(firstSatzart260.getKOSTL()).isNull();
        assertThat(firstSatzart260.getAUFNR()).isNull();
        assertThat(firstSatzart260.getMWSKZ()).isNull();

        assertThat(request.getSatzart010()).isNull();
        assertNothingReported(delivery, SHORT_BATCH, "6 of 6");
    }

    /**
     * What a batch with nothing wrong leaves behind: the file archived as processed, an untouched error
     * trail, one completion line, and no mail of either kind.
     *
     * @param entries how the completion line counts this batch, e.g. {@code 6 of 6}
     */
    private void assertNothingReported(final Delivery delivery, final String batch, final String entries) throws IOException {
        assertThat(delivery.request().getSatzartFehler()).isEmpty();
        assertThat(delivery.request().getSatzart210()).allSatisfy(record -> assertThat(record.getFEHLER()).isNull());
        assertThat(delivery.request().getSatzart260()).allSatisfy(record -> assertThat(record.getFEHLER()).isNull());

        await().atMost(Duration.ofSeconds(15)).until(() -> archived(inbox, "archive/successful", batch));
        assertThat(archived(inbox, "archive/error", batch)).isFalse();

        assertThat(addedTo(ACCOUNT_ERROR_LOG, batch, delivery.accountErrorsBefore()))
                .as("a batch with nothing wrong adds nothing to the error trail")
                .isEmpty();

        final List<String> completions = addedTo(COMPLETION_LOG, batch, delivery.completionsBefore());
        assertThat(completions).singleElement().satisfies(line -> assertThat(line)
                .contains(entries + " entries from the file '" + batch + "' sent to PSCD"));

        // Neither kind: the batch was delivered, and none of its records had to be repaired.
        verifyNoInteractions(this.mailOutPort);
    }
}
