package de.muenchen.refarch.email.integration.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class TemplateError extends RuntimeException {
    public TemplateError(final String message, final Exception cause) {
        super(message, cause);
    }
}
