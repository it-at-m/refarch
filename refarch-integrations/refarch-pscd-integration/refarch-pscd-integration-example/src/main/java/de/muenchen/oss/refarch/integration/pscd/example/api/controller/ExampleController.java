package de.muenchen.oss.refarch.integration.pscd.example.api.controller;

import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart200;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demonstrates library usage: inject the single inbound port and submit a domain batch.
 *
 * <p>
 * The library accepts exactly one datatype: the {@link PscdSatzarten} domain aggregate. Assembling
 * it from whatever wire format the consumer receives is the consumer's concern; here we just build
 * a small sample batch and hand it over.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ExampleController {

    private final SubmitPscdBatchInPort submitPscdBatchInPort;

    @PostMapping("/test/pscd")
    public ResponseEntity<Void> submitSampleBatch(
            @RequestParam(name = "filename", defaultValue = "example.dat") final String filename) {
        log.info("Submitting a sample PSCD batch '{}' via the inbound port", filename);
        this.submitPscdBatchInPort.submit(sampleBatch(filename));
        return ResponseEntity.accepted().build();
    }

    private static PscdSatzarten sampleBatch(final String filename) {
        return PscdSatzarten.builder()
                .filename(filename)
                .satzart010(Satzart010.builder().satzart("010").abstimmsumme("12345").vorzeichen("+").build())
                .satzart200(List.of(Satzart200.builder()
                        .satzart("200").psobkey("OBJ0000000000000001").einnahmeart("0001").betrw("100,00").build()))
                .build();
    }
}
