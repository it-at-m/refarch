package de.muenchen.refarch.integration.address.application.port.in;

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

/**
 * Port to integration infrastructure.
 */
public interface AddressMunichInPort {

    /**
     * Check an address in Munich.
     */
    MuenchenAdresse checkAddress(CheckAddressesModel checkAddressesModel) throws AddressServiceIntegrationException;

    /**
     * List addresses in Munich.
     */
    MuenchenAdresseResponse listAddresses(ListAddressesModel listAddressesModel) throws AddressServiceIntegrationException;

    /**
     * List changes in Munich.
     */
    AenderungResponse listChanges(ListAddressChangesModel listAddressChangesModel) throws AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    MuenchenAdresseResponse searchAddresses(SearchAddressesModel searchAddressesModel) throws AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    AddressDistancesModel searchAddressesGeo(SearchAddressesGeoModel searchAddressesGeoModel) throws AddressServiceIntegrationException;

}
