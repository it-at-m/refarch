package de.muenchen.oss.refarch.integration.pscd.application.port.in;

import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;

/**
 * The single inbound port of the PSCD core.
 *
 * <p>
 * The core accepts exactly one datatype: the domain {@link PscdSatzarten} aggregate.
 * </p>
 */
public interface SubmitPscdBatchInPort {

    /**
     * Submit a fully-assembled PSCD batch for delivery to the PSCD endpoint.
     *
     * @param batch the domain batch to deliver
     */
    void submit(PscdSatzarten batch);
}
