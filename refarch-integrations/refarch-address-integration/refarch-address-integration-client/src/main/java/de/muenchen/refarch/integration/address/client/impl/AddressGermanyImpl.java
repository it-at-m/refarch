package de.muenchen.refarch.integration.address.client.impl;

import de.muenchen.refarch.integration.address.client.api.AddressGermanyApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.gen.api.AdressenBundesweitApi;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AddressGermanyImpl implements AddressGermanyApi {

    private final AdressenBundesweitApi adressenBundesweitApi;

    @Override
    public BundesweiteAdresseResponse searchAddresses(final SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.adressenBundesweitApi.searchAdressen(
                searchAddressesGermanyModel.getQuery(),
                searchAddressesGermanyModel.getZip(),
                searchAddressesGermanyModel.getCityName(),
                searchAddressesGermanyModel.getGemeindeschluessel(),
                searchAddressesGermanyModel.getHouseNumberFilter(),
                searchAddressesGermanyModel.getLetterFilter(),
                searchAddressesGermanyModel.getSort(),
                searchAddressesGermanyModel.getSortdir(),
                searchAddressesGermanyModel.getPage(),
                searchAddressesGermanyModel.getPagesize()).block(), "address bundesweit");
    }

}
