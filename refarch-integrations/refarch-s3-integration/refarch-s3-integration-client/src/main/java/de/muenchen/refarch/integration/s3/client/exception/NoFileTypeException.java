package de.muenchen.refarch.integration.s3.client.exception;

public class NoFileTypeException extends RuntimeException {

    public NoFileTypeException(final String message) {
        super(message);
    }
}
