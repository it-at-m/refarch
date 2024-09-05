package de.muenchen.oss.digiwf.address.integration.application.port.in;

import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;

/**
 * Port to integration infrastructure.
 */
public interface AddressGermanyInPort {

    /**
     * Search for addresses in Germany.
     */
    BundesweiteAdresseResponse searchAddresses(final SearchAddressesGermanyModel searchAddressesGermanyModel)
            throws AddressServiceIntegrationException;

}
