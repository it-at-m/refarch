package de.muenchen.refarch.integration.s3.client.exception;

import java.io.Serial;

public class DocumentStorageException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public DocumentStorageException(final String message, final Exception exception) {
        super(message, exception);
    }

    public DocumentStorageException(final String message) {
        super(message);
    }
}
