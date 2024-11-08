package de.muenchen.refarch.integration.s3.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class FileSizeValidationException extends RuntimeException {
    public FileSizeValidationException(final String message) {
        super(message);
    }
}
