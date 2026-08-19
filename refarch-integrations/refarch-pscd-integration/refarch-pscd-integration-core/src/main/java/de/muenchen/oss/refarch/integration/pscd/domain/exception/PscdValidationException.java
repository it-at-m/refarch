package de.muenchen.oss.refarch.integration.pscd.domain.exception;

/**
 * Raised when an inbound PSCD batch is structurally invalid (e.g. missing the mandatory control
 * record).
 *
 * <p>
 * A <em>caller</em> error, deliberately distinct from a downstream processing/delivery failure
 * ({@link PscdProcessingException}) so inbound adapters can translate it to the right response.
 * </p>
 */
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class PscdValidationException extends PscdProcessingException {

    public PscdValidationException(final String message) {
        super(message);
    }
}
