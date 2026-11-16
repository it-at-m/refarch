package de.muenchen.oss.refarch.integration.pscd.application.service;

import de.muenchen.oss.refarch.integration.pscd.application.port.in.SubmitPscdBatchInPort;
import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service behind the single inbound port.
 *
 * <p>
 * Takes a fully-assembled domain batch from whichever inbound adapter received it (file, SOAP,
 * REST, embedding library) and hands it to the outbound port for delivery.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class SubmitPscdBatchService implements SubmitPscdBatchInPort {

    private final PscdOutPort pscdOutPort;

    @Override
    public void submit(final PscdSatzarten batch) {
        log.info("Delivering PSCD batch '{}'", batch.getFilename());
        this.pscdOutPort.send(batch);
    }
}
