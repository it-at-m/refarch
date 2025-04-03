package de.muenchen.refarch.integration.address.adapter.out.address;

import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.client.api.AddressGermanyApi;
import de.muenchen.refarch.integration.address.client.api.AddressMunichApi;
import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddressClientOutAdapter implements AddressClientOutPort {

    private final AddressGermanyApi addressGermanyApi;
    private final AddressMunichApi addressMunichApi;
    private final StreetsMunichApi streetsMunichApi;

    @Override
    public BundesweiteAdresseResponse searchAddresses(final SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException {
        try {
            return this.addressGermanyApi.searchAddresses(searchAddressesGermanyModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public MuenchenAdresse checkAddress(final CheckAddressesModel checkAddressesModel) throws AddressServiceIntegrationException {
        try {
            return this.addressMunichApi.checkAddress(checkAddressesModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public MuenchenAdresseResponse listAddresses(final ListAddressesModel listAddressesModel) throws AddressServiceIntegrationException {
        try {
            return this.addressMunichApi.listAddresses(listAddressesModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public AenderungResponse listChanges(final ListAddressChangesModel listAddressChangesModel) throws AddressServiceIntegrationException {
        try {
            return this.addressMunichApi.listChanges(listAddressChangesModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public MuenchenAdresseResponse searchAddresses(final SearchAddressesModel searchAddressesModel)
            throws AddressServiceIntegrationException {
        try {
            return this.addressMunichApi.searchAddresses(searchAddressesModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public AddressDistancesModel searchAddressesGeo(final SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationException {
        try {
            return this.addressMunichApi.searchAddressesGeo(searchAddressesGeoModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public Strasse findStreetsById(final long streetId) throws AddressServiceIntegrationException {
        try {
            return this.streetsMunichApi.findStreetsById(streetId);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

    @Override
    public StrasseResponse listStreets(final ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException {
        try {
            return this.streetsMunichApi.listStreets(listStreetsModel);
        } catch (final
                AddressServiceIntegrationException | AddressServiceIntegrationServerErrorException | AddressServiceIntegrationClientErrorException exception) {
            throw new AddressServiceIntegrationException(exception.getMessage(), exception);
        }
    }

}
