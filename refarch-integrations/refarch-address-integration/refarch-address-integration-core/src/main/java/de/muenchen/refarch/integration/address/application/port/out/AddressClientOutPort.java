package de.muenchen.refarch.integration.address.application.port.out;

import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;

/**
 * Port to integration infrastructure.
 */
public interface AddressClientOutPort {

    /**
     * Search for addresses in Germany.
     */
    BundesweiteAdresseResponse searchAddresses(SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException;

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
    AddressDistancesModel searchAddressesGeo(SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    Strasse findStreetsById(long streetId) throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    StrasseResponse listStreets(ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException;

}
