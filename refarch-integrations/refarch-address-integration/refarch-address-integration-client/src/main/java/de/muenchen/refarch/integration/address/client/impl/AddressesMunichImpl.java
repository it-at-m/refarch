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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AddressesMunichImpl implements AddressMunichApi {

    public static final String TYPE = "address";
    private final AdressenMnchenApi adressenMuenchenApi;

    @Override
    public MuenchenAdresse checkAddress(final CheckAddressesModel checkAddressesModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.adressenMuenchenApi.checkAdresse(
                checkAddressesModel.getAddress(),
                checkAddressesModel.getStreetName(),
                checkAddressesModel.getStreetId(),
                checkAddressesModel.getHouseNumber(),
                checkAddressesModel.getAdditionalInfo(),
                checkAddressesModel.getZip(),
                checkAddressesModel.getCityName(),
                checkAddressesModel.getGemeindeschluessel()).block(), TYPE);
    }

    @Override
    public MuenchenAdresseResponse listAddresses(final ListAddressesModel listAddressesModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.adressenMuenchenApi.listAdressen(
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
                listAddressesModel.getPagesize()).block(), TYPE);
    }

    @Override
    public AenderungResponse listChanges(final ListAddressChangesModel listAddressChangesModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.adressenMuenchenApi.listAenderungen(
                listAddressChangesModel.getEffectiveDateFrom(),
                listAddressChangesModel.getEffectiveDateTo(),
                listAddressChangesModel.getStreetName(),
                listAddressChangesModel.getHouseNumber(),
                listAddressChangesModel.getZip(),
                listAddressChangesModel.getAdditionalInfo(),
                listAddressChangesModel.getSorting(),
                listAddressChangesModel.getSortingDir(),
                listAddressChangesModel.getPageNumber(),
                listAddressChangesModel.getPageSize()).block(), TYPE);
    }

    @Override
    public MuenchenAdresseResponse searchAddresses(final SearchAddressesModel searchAddressesModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.adressenMuenchenApi.searchAdressen1(
                searchAddressesModel.getQuery(),
                searchAddressesModel.getZipFilter(),
                searchAddressesModel.getHouseNumberFilter(),
                searchAddressesModel.getLetterFilter(),
                searchAddressesModel.getSearchtype(),
                searchAddressesModel.getSort(),
                searchAddressesModel.getSortdir(),
                searchAddressesModel.getPage(),
                searchAddressesModel.getPagesize()).block(), TYPE);
    }

    @Override
    public AddressDistancesModel searchAddressesGeo(final SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        return ExceptionHandler.executeWithErrorHandling(() -> {
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
        }, TYPE);
    }
}
