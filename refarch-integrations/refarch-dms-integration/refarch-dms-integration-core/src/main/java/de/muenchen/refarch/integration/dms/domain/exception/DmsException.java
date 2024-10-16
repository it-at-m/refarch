package de.muenchen.refarch.integration.dms.domain.exception;

import java.io.Serial;
import lombok.Getter;

@Getter
public class DmsException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String statusCode;

    public DmsException(final String statusCode, final String message) {
        super(String.format("%s: %s", statusCode, message));
        this.statusCode = statusCode;
    }
}
