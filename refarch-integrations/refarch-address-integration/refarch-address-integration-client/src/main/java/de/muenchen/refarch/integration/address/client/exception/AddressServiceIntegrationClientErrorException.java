package de.muenchen.refarch.integration.address.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class AddressServiceIntegrationClientErrorException extends Exception {

    public AddressServiceIntegrationClientErrorException(final String message, final Exception exception) {
        super(message, exception);
    }

}
