package de.muenchen.refarch.integration.cosys.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class DocumentGenerationException extends Exception {
    public DocumentGenerationException(final String message) {
        super(message);
    }

    public DocumentGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
