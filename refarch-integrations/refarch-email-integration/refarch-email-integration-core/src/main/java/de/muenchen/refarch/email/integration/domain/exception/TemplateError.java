package de.muenchen.refarch.email.integration.domain.exception;

public class TemplateError extends Error {
    public TemplateError(final String message) {
        super(message);
    }
}
