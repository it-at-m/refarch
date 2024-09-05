package de.muenchen.refarch.integration.address.client.api;

import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;

/**
 * This interface defines the methods used for the communication with the address service.
 */
public interface AddressGermanyApi {

    /**
     * This method searches for addresses in the address service.
     *
     * @param searchAddressesGermanyModel
     * @return
     * @throws AddressServiceIntegrationClientErrorException
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     */
    BundesweiteAdresseResponse searchAddresses(final SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException;

}
