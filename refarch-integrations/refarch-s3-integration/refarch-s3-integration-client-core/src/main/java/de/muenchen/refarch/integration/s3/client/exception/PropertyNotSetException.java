package de.muenchen.refarch.integration.s3.client.exception;

import java.io.Serial;

public class PropertyNotSetException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public PropertyNotSetException(final String message) {
        super(message);
    }

}
