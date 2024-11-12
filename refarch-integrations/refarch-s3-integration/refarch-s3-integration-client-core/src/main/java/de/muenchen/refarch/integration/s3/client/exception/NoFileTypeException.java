package de.muenchen.refarch.integration.s3.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class NoFileTypeException extends RuntimeException {
    public NoFileTypeException(final String message) {
        super(message);
    }

    public NoFileTypeException(final String message, final Exception e) {
        super(message, e);
    }
}
