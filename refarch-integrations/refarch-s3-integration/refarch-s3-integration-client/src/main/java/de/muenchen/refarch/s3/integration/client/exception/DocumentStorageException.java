package de.muenchen.refarch.s3.integration.client.exception;

public class DocumentStorageException extends Exception {

    public DocumentStorageException(final String message, final Exception exception) {
        super(message, exception);
    }

}
