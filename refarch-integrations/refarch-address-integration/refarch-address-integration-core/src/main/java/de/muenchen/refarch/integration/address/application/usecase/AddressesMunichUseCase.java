package de.muenchen.refarch.integration.address.application.usecase;

import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.application.port.in.AddressMunichInPort;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressesMunichUseCase implements AddressMunichInPort {

    private final AddressClientOutPort addressClientOutPort;

    @Override
    public MuenchenAdresse checkAddress(CheckAddressesModel checkAddressesModel) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.checkAddress(checkAddressesModel);
    }

    @Override
    public MuenchenAdresseResponse listAddresses(ListAddressesModel listAddressesModel) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.listAddresses(listAddressesModel);
    }

    @Override
    public AenderungResponse listChanges(ListAddressChangesModel listAddressChangesModel) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.listChanges(listAddressChangesModel);
    }

    @Override
    public MuenchenAdresseResponse searchAddresses(SearchAddressesModel searchAddressesModel) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.searchAddresses(searchAddressesModel);
    }

    @Override
    public AddressDistancesModel searchAddressesGeo(SearchAddressesGeoModel searchAddressesGeoModel) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.searchAddressesGeo(searchAddressesGeoModel);
    }

}
