package de.muenchen.refarch.integration.dms.domain.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class DmsException extends Exception {
    private final String statusCode;

    public DmsException(final String statusCode, final String message) {
        super(String.format("%s: %s", statusCode, message));
        this.statusCode = statusCode;
    }
}
