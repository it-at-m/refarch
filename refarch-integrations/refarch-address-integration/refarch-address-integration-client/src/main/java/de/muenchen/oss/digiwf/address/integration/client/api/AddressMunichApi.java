package de.muenchen.oss.digiwf.address.integration.client.api;

import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationServerErrorException;
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
 * This interface defines the methods used for the communication with the address service.
 */
public interface AddressMunichApi {

    /**
     * This method checks an address in the address service.
     *
     * @param checkAddressesModel
     * @return
     * @throws AddressServiceIntegrationClientErrorException
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     */
    MuenchenAdresse checkAddress(final CheckAddressesModel checkAddressesModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException;

    /**
     * This method lists addresses in the address service.
     *
     * @param listAddressesModel
     * @return
     * @throws AddressServiceIntegrationClientErrorException
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     */
    MuenchenAdresseResponse listAddresses(final ListAddressesModel listAddressesModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException;

    /**
     * This method lists changes of an address in the address service.
     *
     * @param listAddressChangesModel
     * @return
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     * @throws AddressServiceIntegrationClientErrorException
     */
    AenderungResponse listChanges(final ListAddressChangesModel listAddressChangesModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException;

    /**
     * This method searches for addresses in the address service.
     *
     * @param searchAddressesModel
     * @return
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     * @throws AddressServiceIntegrationClientErrorException
     */
    MuenchenAdresseResponse searchAddresses(final SearchAddressesModel searchAddressesModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException;

    /**
     * This method searches for addresses in the address service by the geo location.
     *
     * @param searchAddressesGeoModel
     * @return
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     * @throws AddressServiceIntegrationClientErrorException
     */
    AddressDistancesModel searchAddressesGeo(final SearchAddressesGeoModel searchAddressesGeoModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException;
}
