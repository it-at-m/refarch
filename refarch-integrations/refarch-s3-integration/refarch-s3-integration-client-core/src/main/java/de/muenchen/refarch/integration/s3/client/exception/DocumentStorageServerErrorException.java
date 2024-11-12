package de.muenchen.refarch.integration.s3.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class DocumentStorageServerErrorException extends Exception {
    public DocumentStorageServerErrorException(final String message, final Exception exception) {
        super(message, exception);
    }

}
