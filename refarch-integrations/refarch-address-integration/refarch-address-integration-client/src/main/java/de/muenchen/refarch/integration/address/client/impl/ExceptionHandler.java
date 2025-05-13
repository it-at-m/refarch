package de.muenchen.refarch.integration.address.client.impl;

import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@Slf4j
final class ExceptionHandler {
    public static final String REQUEST_EXCEPTION_MESSAGE = "The request to get %s failed with %s. %s";
    public static final String REQUEST_EXCEPTION_MESSAGE_SHORT = "The request to get %s failed.";

    private ExceptionHandler() {
    }

    public static <K> K executeWithErrorHandling(final Supplier<K> callable, final String type)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException {
        try {
            return callable.get();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, type, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, type, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE_SHORT, type);
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }
}
