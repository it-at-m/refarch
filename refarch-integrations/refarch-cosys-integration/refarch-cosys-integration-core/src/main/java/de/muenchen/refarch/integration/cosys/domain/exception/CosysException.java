package de.muenchen.refarch.integration.cosys.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class CosysException extends Exception {
    public CosysException(final String message) {
        super(message);
    }

    public CosysException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
