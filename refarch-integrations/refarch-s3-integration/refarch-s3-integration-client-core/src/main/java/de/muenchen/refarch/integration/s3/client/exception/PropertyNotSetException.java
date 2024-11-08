package de.muenchen.refarch.integration.s3.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class PropertyNotSetException extends Exception {
    public PropertyNotSetException(final String message) {
        super(message);
    }

}
