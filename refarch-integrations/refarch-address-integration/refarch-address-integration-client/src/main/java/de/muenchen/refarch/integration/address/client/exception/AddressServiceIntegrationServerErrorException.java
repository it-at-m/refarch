package de.muenchen.refarch.integration.address.client.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class AddressServiceIntegrationServerErrorException extends Exception {

    public AddressServiceIntegrationServerErrorException(final String message, final Exception exception) {
        super(message, exception);
    }

}
