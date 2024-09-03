package de.muenchen.refarch.integration.dms.domain.exception;

public class DmsException extends Exception{
    public DmsException(final String statusCode, final String message) {
        super(String.format("%s: %s", statusCode, message));
    }
}
