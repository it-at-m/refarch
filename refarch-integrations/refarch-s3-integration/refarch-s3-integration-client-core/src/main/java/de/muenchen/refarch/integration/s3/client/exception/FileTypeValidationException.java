package de.muenchen.refarch.integration.s3.client.exception;

public class FileTypeValidationException extends RuntimeException {
    public FileTypeValidationException(final String message) {
        super(message);
    }
}
