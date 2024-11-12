package de.muenchen.refarch.integration.address.client.impl;

import de.muenchen.refarch.integration.address.client.api.AddressMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import de.muenchen.refarch.integration.address.client.gen.api.AdressenMnchenApi;
import de.muenchen.refarch.integration.address.client.gen.model.AdresseDistanz;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
@Slf4j
public class AddressesMunichImpl implements AddressMunichApi {

    public static final String REQUEST_EXCEPTION_MESSAGE = "The request to get address failed with %s. %s";
    public static final String REQUEST_EXCEPTION_MESSAGE_SHORT = "The request to get address failed.";
    private final AdressenMnchenApi adressenMuenchenApi;

    @Override
    public MuenchenAdresse checkAddress(final CheckAddressesModel checkAddressesModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException {
        try {
            return this.adressenMuenchenApi.checkAdresse(
                    checkAddressesModel.getAddress(),
                    checkAddressesModel.getStreetName(),
                    checkAddressesModel.getStreetId(),
                    checkAddressesModel.getHouseNumber(),
                    checkAddressesModel.getAdditionalInfo(),
                    checkAddressesModel.getZip(),
                    checkAddressesModel.getCityName(),
                    checkAddressesModel.getGemeindeschluessel()).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = REQUEST_EXCEPTION_MESSAGE_SHORT;
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }

    @Override
    public MuenchenAdresseResponse listAddresses(final ListAddressesModel listAddressesModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException {
        try {
            return this.adressenMuenchenApi.listAdressen(
                    listAddressesModel.getBaublock(),
                    listAddressesModel.getErhaltungssatzung(),
                    listAddressesModel.getGemarkung(),
                    listAddressesModel.getKaminkehrerbezirk(),
                    listAddressesModel.getZip(),
                    listAddressesModel.getMittelschule(),
                    listAddressesModel.getGrundschule(),
                    listAddressesModel.getPolizeiinspektion(),
                    listAddressesModel.getStimmbezirk(),
                    listAddressesModel.getStimmkreis(),
                    listAddressesModel.getWahlbezirk(),
                    listAddressesModel.getWahlkreis(),
                    listAddressesModel.getStadtbezirk(),
                    listAddressesModel.getStadtbezirksteil(),
                    listAddressesModel.getStadtbezirksviertel(),
                    listAddressesModel.getSort(),
                    listAddressesModel.getSortdir(),
                    listAddressesModel.getPage(),
                    listAddressesModel.getPagesize()).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = REQUEST_EXCEPTION_MESSAGE_SHORT;
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }

    @Override
    public AenderungResponse listChanges(final ListAddressChangesModel listAddressChangesModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        try {
            return this.adressenMuenchenApi.listAenderungen(
                    listAddressChangesModel.getEffectiveDateFrom(),
                    listAddressChangesModel.getEffectiveDateTo(),
                    listAddressChangesModel.getStreetName(),
                    listAddressChangesModel.getHouseNumber(),
                    listAddressChangesModel.getZip(),
                    listAddressChangesModel.getAdditionalInfo(),
                    listAddressChangesModel.getSorting(),
                    listAddressChangesModel.getSortingDir(),
                    listAddressChangesModel.getPageNumber(),
                    listAddressChangesModel.getPageSize()).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = REQUEST_EXCEPTION_MESSAGE_SHORT;
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }

    @Override
    public MuenchenAdresseResponse searchAddresses(final SearchAddressesModel searchAddressesModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        try {
            return this.adressenMuenchenApi.searchAdressen1(
                    searchAddressesModel.getQuery(),
                    searchAddressesModel.getZipFilter(),
                    searchAddressesModel.getHouseNumberFilter(),
                    searchAddressesModel.getLetterFilter(),
                    searchAddressesModel.getSearchtype(),
                    searchAddressesModel.getSort(),
                    searchAddressesModel.getSortdir(),
                    searchAddressesModel.getPage(),
                    searchAddressesModel.getPagesize()).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            log.debug(message);
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = REQUEST_EXCEPTION_MESSAGE_SHORT;
            log.debug(message);
            throw new AddressServiceIntegrationException(message, exception);
        }
    }

    @Override
    public AddressDistancesModel searchAddressesGeo(final SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        try {
            final List<AdresseDistanz> addressDistances = this.adressenMuenchenApi.searchAdressenGeo(
                    searchAddressesGeoModel.getGeometry(),
                    searchAddressesGeoModel.getLat(),
                    searchAddressesGeoModel.getLng(),
                    searchAddressesGeoModel.getDistance(),
                    searchAddressesGeoModel.getTopleftlat(),
                    searchAddressesGeoModel.getTopleftlng(),
                    searchAddressesGeoModel.getBottomrightlat(),
                    searchAddressesGeoModel.getBottomrightlng(),
                    searchAddressesGeoModel.getFormat()).toStream().toList();
            return AddressDistancesModel.builder()
                    .addressDistances(addressDistances)
                    .build();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            throw new AddressServiceIntegrationClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format(REQUEST_EXCEPTION_MESSAGE, exception.getStatusCode(), exception.getMessage());
            throw new AddressServiceIntegrationServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            throw new AddressServiceIntegrationException(REQUEST_EXCEPTION_MESSAGE_SHORT, exception);
        }
    }
}
