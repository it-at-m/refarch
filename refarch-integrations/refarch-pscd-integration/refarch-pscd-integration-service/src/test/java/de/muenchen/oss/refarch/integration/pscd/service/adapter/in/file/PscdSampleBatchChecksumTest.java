package de.muenchen.oss.refarch.integration.pscd.service.adapter.in.file;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart200;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart210;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart250;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart260;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * What pins the transcribed column table to the spec: the complete sample batch brought its own
 * checksum, and the checksum only adds up if the amounts were read from the right characters.
 *
 * <p>
 * Every other parser test builds its fixtures <em>from</em> the layout table (see
 * {@link PscdRecordFixtures}) and so cannot catch a wrong table. The little that is checkable
 * without the spec at hand, positions that cannot exist, leaves most of it open: gaps are expected,
 * because the record carries fields this integration does not map, and shared columns are
 * legitimate
 * by spec. This test is the independent cross-check, because {@code ABSTIMMSUMME} was computed by
 * the predecessor and not by this parser: 808 amounts of four record types, read at four offsets,
 * one of them ({@code 250}, columns 73-83) nowhere near the other three, reconcile to its ten-digit
 * total. A misread column does not do that by accident.
 * </p>
 *
 * <p>
 * <strong>The reconciliation below is an assumption, not a documented rule.</strong> Nothing
 * available here says how {@code ABSTIMMSUMME} is formed: neither the record layout nor the vendor
 * WSDL specifies it, and the signs are simply the combination under which this sample adds up,
 * found
 * by trying them. That is all this test needs of them, because what it checks is the column table
 * and
 * not the accounting: whether {@code 250} is a credit by nature is the accounting side's business,
 * that its amount lands where the layout says is this test's. It does mean a failure here has two
 * possible causes, and the assumption is the one to rule out second: a column read from the wrong
 * place, or a batch whose checksum was formed by a different rule than this one sample's.
 * </p>
 *
 * <p>
 * The total alone has one blind spot, which is why the widths are checked too: the amounts are
 * zero-padded to their full column width, so a window shifted one place into the blank that follows
 * loses a leading zero and gains padding that is stripped again, leaving the same number. An amount
 * that arrives shorter than its column is exactly that, and nothing else.
 * </p>
 *
 * <p>
 * The sample is read exactly as it is, and decoded the way the poller decodes it.
 * </p>
 */
class PscdSampleBatchChecksumTest {

    /** The one sample covering every record type, as the predecessor produced it. */
    private static final String BATCH = "d_gws_01_fwpkfbp0_20190329_w01_buchungssaetze";

    private static final String BETRW = "BETRW";

    @Test
    void theSampleBatchsOwnChecksumReconcilesWithTheParsedAmounts() throws IOException {
        final PscdSatzarten batch = PscdSatzartenParser.parse(sampleLines(), BATCH);

        final Map<String, List<String>> amounts = new LinkedHashMap<>();
        amounts.put("200", amountsOf(batch.getSatzart200(), Satzart200::getBetrw));
        amounts.put("210", amountsOf(batch.getSatzart210(), Satzart210::getBetrw));
        amounts.put("250", amountsOf(batch.getSatzart250(), Satzart250::getBetrw));
        amounts.put("260", amountsOf(batch.getSatzart260(), Satzart260::getBetrw));

        assertThat(amounts).allSatisfy((code, values) -> assertThat(values)
                .as("SATZART %s amounts, each filling the %d characters its BETRW column spans", code, betrwWidth(code))
                .isNotEmpty()
                .allMatch(value -> value.matches("\\d{%d}".formatted(betrwWidth(code)))));

        // Assumed, not specified: how ABSTIMMSUMME is formed is documented nowhere available here, so
        // these signs are inferred from this one sample. See the class Javadoc.
        final long reconciled = total(amounts.get("200")) - total(amounts.get("210"))
                - total(amounts.get("250")) + total(amounts.get("260"));

        assertThat(batch.getSatzart010().getVorzeichen())
                .as("the checksum's own sign, so its digits are the net total the amounts have to reach")
                .isEqualTo("+");
        assertThat(reconciled)
                .as("SATZART 010 ABSTIMMSUMME reconciled against the batch's 808 amounts")
                .isEqualTo(Long.parseLong(batch.getSatzart010().getAbstimmsumme()));
    }

    /**
     * The amounts of one record type, as the parser read them. Taken from the parsed records rather
     * than sliced out of the lines a second time: a test that repeated the offsets would agree with a
     * wrong table.
     *
     * <p>
     * {@code BETRW} is mandatory on all four types, so an amount read from the wrong place fails here
     * either way: as a total that does not reconcile, or, where the characters are blank, as the
     * {@code REQUIRED} the parser fills in, which is neither the right width nor a number.
     * </p>
     */
    private static <T> List<String> amountsOf(final List<T> records, final Function<T, String> betrw) {
        return records.stream().map(betrw).toList();
    }

    private static long total(final List<String> amounts) {
        return amounts.stream().mapToLong(Long::parseLong).sum();
    }

    /** How many characters the layout gives BETRW on one record type, which a value has to fill. */
    private static int betrwWidth(final String code) {
        return PscdSatzartenParser.layoutsByCode().get(code).columns().stream()
                .filter(column -> BETRW.equals(column.name()))
                .mapToInt(column -> column.end() - column.start() + 1)
                .findFirst()
                .orElseThrow();
    }

    /**
     * The sample's lines, decoded as {@code PscdFilePoller} decodes a polled file: the host export's
     * charset, which {@code PscdInboundProperties} defaults to, and the file's CRLF line endings.
     */
    private static List<String> sampleLines() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("pscd/" + BATCH).getInputStream(), StandardCharsets.ISO_8859_1))) {
            return reader.lines().toList();
        }
    }
}
