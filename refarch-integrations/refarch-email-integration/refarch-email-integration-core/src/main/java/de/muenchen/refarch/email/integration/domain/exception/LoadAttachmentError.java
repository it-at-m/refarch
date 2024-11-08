package de.muenchen.refarch.email.integration.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class LoadAttachmentError extends RuntimeException {
    public LoadAttachmentError(final String message) {
        super(message);
    }

    public LoadAttachmentError(final String message, final Exception cause) {
        super(message, cause);
    }
}
