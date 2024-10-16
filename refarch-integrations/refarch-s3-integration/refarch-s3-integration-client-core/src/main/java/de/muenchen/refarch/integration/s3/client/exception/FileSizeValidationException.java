package de.muenchen.refarch.integration.s3.client.exception;

import java.io.Serial;

public class FileSizeValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileSizeValidationException(final String message) {
        super(message);
    }
}
