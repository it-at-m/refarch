package de.muenchen.refarch.email.integration.domain.exception;

import java.io.Serial;

public class LoadAttachmentError extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public LoadAttachmentError(final String message) {
        super(message);
    }

    public LoadAttachmentError(final String message, final Exception cause) {
        super(message, cause);
    }
}
