package de.muenchen.oss.refarch.integration.pscd.adapter.out.pscd;

import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.client.PscdSoapClient;
import de.muenchen.oss.refarch.integration.pscd.domain.exception.PscdProcessingException;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbound adapter implementing {@link PscdOutPort}.
 *
 * <p>
 * Maps the {@link PscdSatzarten} domain batch onto the SOAP contract ({@link DTSOAPSatzarten}) via
 * {@link PscdSatzartenMapper} and delegates the one-way call to the {@link PscdSoapClient}.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class PscdOutAdapter implements PscdOutPort {

    private final PscdSoapClient pscdSoapClient;

    @Override
    public void send(final PscdSatzarten satzarten) {
        try {
            final DTSOAPSatzarten request = PscdSatzartenMapper.toDto(satzarten);
            this.pscdSoapClient.send(request);
            log.debug("Sent PSCD batch '{}' to the SOAP endpoint", satzarten.getFilename());
        } catch (final RuntimeException e) {
            throw new PscdProcessingException("Failed to call the PSCD SOAP endpoint", e);
        }
    }
}
