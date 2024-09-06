package de.muenchen.refarch.email.integration.domain.exception;

import java.io.Serial;

public class TemplateError extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public TemplateError(final String message, final Exception cause) {
        super(message, cause);
    }
}
