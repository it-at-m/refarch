package de.muenchen.oss.refarch.integration.pscd.application.port.out;

import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;

/**
 * Outbound port the transformation route terminates in (via {@code bean:pscdOutPort}).
 *
 * <p>
 * Expressed purely in domain terms ({@link PscdSatzarten}). The implementing adapter is responsible
 * for mapping the domain batch onto the SOAP contract and delivering it.
 * </p>
 */
public interface PscdOutPort {

    /**
     * Deliver a transformed PSCD batch to the PSCD SOAP endpoint.
     *
     * @param satzarten the domain batch to send
     */
    void send(PscdSatzarten satzarten);
}
