package de.muenchen.refarch.integration.address.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class AddressServiceIntegrationException extends Exception {

    public AddressServiceIntegrationException(final String message, final Exception exception) {
        super(message, exception);
    }

}
