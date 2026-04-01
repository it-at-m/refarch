package de.muenchen.oss.refarch.integration.address.application.port.out;

import de.muenchen.oss.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.oss.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.oss.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.oss.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import de.muenchen.oss.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.oss.refarch.integration.address.client.gen.model.StrasseResponse;
import de.muenchen.oss.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.oss.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.oss.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.oss.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.oss.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.oss.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.oss.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.oss.refarch.integration.address.client.model.response.AddressDistancesModel;

/**
 * Port to integration infrastructure.
 */
public interface AddressOutPort {

    /**
     * Search for addresses in Germany.
     */
    BundesweiteAdresseResponse searchGermanyAddresses(SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException;

    /**
     * Check an address in Munich.
     */
    MuenchenAdresse checkMunichAddress(CheckAddressesModel checkAddressesModel) throws AddressServiceIntegrationException;

    /**
     * List addresses in Munich.
     */
    MuenchenAdresseResponse listMunichAddresses(ListAddressesModel listAddressesModel) throws AddressServiceIntegrationException;

    /**
     * List changes in Munich.
     */
    AenderungResponse listMunichChanges(ListAddressChangesModel listAddressChangesModel) throws AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    MuenchenAdresseResponse searchMunichAddresses(SearchAddressesModel searchAddressesModel) throws AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    AddressDistancesModel searchMunichAddressesGeo(SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    Strasse findMunichStreetsById(long streetId) throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    StrasseResponse listMunichStreets(ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException;

}
