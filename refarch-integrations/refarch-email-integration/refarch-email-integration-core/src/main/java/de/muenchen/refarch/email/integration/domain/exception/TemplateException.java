package de.muenchen.refarch.email.integration.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class TemplateException extends RuntimeException {
    public TemplateException(final String message, final Exception cause) {
        super(message, cause);
    }
}
