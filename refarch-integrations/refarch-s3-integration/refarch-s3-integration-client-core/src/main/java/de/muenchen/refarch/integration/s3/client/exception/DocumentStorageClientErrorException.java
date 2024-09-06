package de.muenchen.refarch.integration.s3.client.exception;

import java.io.Serial;

public class DocumentStorageClientErrorException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public DocumentStorageClientErrorException(final String message, final Exception exception) {
        super(message, exception);
    }

}
