package de.muenchen.oss.refarch.integration.pscd.domain.exception;

/**
 * Raised when a PSCD record cannot be processed or delivered to the SOAP endpoint.
 */
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class PscdProcessingException extends RuntimeException {

    public PscdProcessingException(final String message) {
        super(message);
    }

    public PscdProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
