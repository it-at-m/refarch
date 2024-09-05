package de.muenchen.oss.digiwf.address.integration.application.port.out;

import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.CheckAddressesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListAddressChangesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListAddressesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListStreetsModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesGeoModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesModel;
import de.muenchen.oss.digiwf.address.integration.client.model.response.AddressDistancesModel;
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
    BundesweiteAdresseResponse searchAddresses(final SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException;

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
    MuenchenAdresseResponse searchAddresses(final SearchAddressesModel searchAddressesModel) throws
            AddressServiceIntegrationException;

    /**
     * Search for addresses in Munich.
     */
    AddressDistancesModel searchAddressesGeo(final SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    Strasse findStreetsById(final long streetId) throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    StrasseResponse listStreets(final ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException;

}
