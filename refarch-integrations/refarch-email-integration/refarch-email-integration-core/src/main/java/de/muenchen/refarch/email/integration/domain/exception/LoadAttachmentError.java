package de.muenchen.refarch.email.integration.domain.exception;

public class LoadAttachmentError extends Error {
    public LoadAttachmentError(final String message) {
        super(message);
    }
}
