package de.muenchen.oss.digiwf.address.integration.application.port.in;

import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.CheckAddressesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListAddressChangesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListAddressesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesGeoModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.response.AddressDistancesModel;
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
    MuenchenAdresse checkAddress(final CheckAddressesModel checkAddressesModel) throws AddressServiceIntegrationException;

    /**
     * List addresses in Munich.
     */
    MuenchenAdresseResponse listAddresses(final ListAddressesModel listAddressesModel) throws AddressServiceIntegrationException;

    /**
     * List changes in Munich.
     */
    AenderungResponse listChanges(final ListAddressChangesModel listAddressChangesModel) throws AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    MuenchenAdresseResponse searchAddresses(final SearchAddressesModel searchAddressesModel) throws AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    AddressDistancesModel searchAddressesGeo(final SearchAddressesGeoModel searchAddressesGeoModel) throws AddressServiceIntegrationException;

}
