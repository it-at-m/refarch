package de.muenchen.refarch.integration.address.application.usecase;

import de.muenchen.refarch.integration.address.application.port.in.AddressGermanyInPort;
import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressesGermanyUseCase implements AddressGermanyInPort {

    private final AddressClientOutPort addressClientOutPort;

    @Override
    public BundesweiteAdresseResponse searchAddresses(SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException {
        return addressClientOutPort.searchAddresses(searchAddressesGermanyModel);
    }

}
