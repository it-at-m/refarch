package de.muenchen.refarch.integration.address.application.port.in;

import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;

/**
 * Port to integration infrastructure.
 */
public interface AddressGermanyInPort {

    /**
     * Search for addresses in Germany.
     */
    BundesweiteAdresseResponse searchAddresses(SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException;

}
