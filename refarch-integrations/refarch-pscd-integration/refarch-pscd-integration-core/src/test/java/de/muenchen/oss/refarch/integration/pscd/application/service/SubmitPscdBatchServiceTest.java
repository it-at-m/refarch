package de.muenchen.oss.refarch.integration.pscd.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubmitPscdBatchServiceTest {

    @Test
    void handsTheSubmittedBatchToTheOutPort() {
        final List<PscdSatzarten> delivered = new ArrayList<>();
        // PscdOutPort is a single-method port; a capturing lambda stands in for the outbound adapter.
        final SubmitPscdBatchService service = new SubmitPscdBatchService(delivered::add);

        final PscdSatzarten batch = PscdSatzarten.builder()
                .filename("records.dat")
                .satzart010(Satzart010.builder().satzart("010").abstimmsumme("12345").build())
                .build();

        service.submit(batch);

        assertThat(delivered).singleElement().isSameAs(batch);
    }
}
