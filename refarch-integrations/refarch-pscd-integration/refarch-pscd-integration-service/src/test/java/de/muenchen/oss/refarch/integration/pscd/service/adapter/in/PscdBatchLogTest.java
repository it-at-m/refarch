package de.muenchen.oss.refarch.integration.pscd.service.adapter.in;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart100;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart250;
import de.muenchen.oss.refarch.integration.pscd.domain.model.SatzartFehler;
import java.util.List;
import org.junit.jupiter.api.Test;

class PscdBatchLogTest {

    @Test
    void describesRecordCountsAndTheControlValues() {
        final PscdSatzarten batch = PscdSatzarten.builder()
                .satzart010(Satzart010.builder().abstimmsumme("12345").vorzeichen("+").build())
                .satzart100(List.of(Satzart100.builder().build(), Satzart100.builder().build()))
                .satzart250(List.of(Satzart250.builder().build()))
                .build();

        assertThat(PscdBatchLog.describe(batch)).isEqualTo("4 records (010=1, 100=2, 250=1), abstimmsumme='12345' vorzeichen='+'");
    }

    @Test
    void describesAnEmptyBatchAndAMissingControlRecord() {
        assertThat(PscdBatchLog.describe(PscdSatzarten.builder().build())).isEqualTo("0 records, no control record");
        assertThat(PscdBatchLog.describe(null)).isEqualTo("no batch");
    }

    @Test
    void countsInBandErrorRecordsWithoutRepeatingTheirText() {
        final PscdSatzarten batch = PscdSatzarten.builder()
                .satzart010(Satzart010.builder().abstimmsumme("7").vorzeichen("-").build())
                .satzartFehler(List.of(SatzartFehler.builder().satzart("200").fehlertext("posting rejected").build()))
                .build();

        // Vendor free text may echo personal or financial data, so only the count is logged.
        assertThat(PscdBatchLog.describe(batch))
                .isEqualTo("2 records (010=1, FEHLER=1), abstimmsumme='7' vorzeichen='-'")
                .doesNotContain("posting rejected");
    }
}
