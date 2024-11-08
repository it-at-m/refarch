package de.muenchen.refarch.integration.s3.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class FileTypeValidationException extends RuntimeException {
    public FileTypeValidationException(final String message) {
        super(message);
    }
}
