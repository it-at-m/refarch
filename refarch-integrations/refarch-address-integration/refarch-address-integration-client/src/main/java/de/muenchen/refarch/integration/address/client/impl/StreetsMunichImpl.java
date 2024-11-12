package de.muenchen.refarch.integration.address.client.impl;

import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.api.StraenMnchenApi;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
@Slf4j
public class StreetsMunichImpl implements StreetsMunichApi {

    public static final String REQUEST_EXCEPTION_MESSAGE = "The request to get street failed with %s. %s";
    private final StraenMnchenApi straessenMuenchenApi;

    @Override
    public Strasse findStreetsById(final long streetId)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        try {
            return this.straessenMuenchenApi.findStrasseByNummer(streetId).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to get street failed.";
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }

    @Override
    public StrasseResponse listStreets(final ListStreetsModel listStreetsModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        try {
            return this.straessenMuenchenApi.listStrassen(
                    listStreetsModel.getCityDistrictNames(),
                    listStreetsModel.getCityDistrictNumbers(),
                    listStreetsModel.getStreetName(),
                    listStreetsModel.getSortdir(),
                    listStreetsModel.getPage(),
                    listStreetsModel.getPagesize()).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to get street failed.";
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }

}
