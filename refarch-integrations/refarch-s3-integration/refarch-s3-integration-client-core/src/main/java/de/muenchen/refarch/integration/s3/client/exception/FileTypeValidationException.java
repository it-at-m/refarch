package de.muenchen.refarch.integration.s3.client.exception;

import java.io.Serial;

public class FileTypeValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileTypeValidationException(final String message) {
        super(message);
    }
}
