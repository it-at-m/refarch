package de.muenchen.oss.digiwf.address.integration.application.usecase;

import de.muenchen.oss.digiwf.address.integration.application.port.in.AddressGermanyInPort;
import de.muenchen.oss.digiwf.address.integration.application.port.out.AddressClientOutPort;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesGermanyModel;
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
