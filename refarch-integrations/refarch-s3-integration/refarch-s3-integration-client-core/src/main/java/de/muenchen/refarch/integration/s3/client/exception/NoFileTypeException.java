package de.muenchen.refarch.integration.s3.client.exception;

import java.io.Serial;

public class NoFileTypeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NoFileTypeException(final String message) {
        super(message);
    }

    public NoFileTypeException(final String message, final Exception e) {
        super(message, e);
    }
}
