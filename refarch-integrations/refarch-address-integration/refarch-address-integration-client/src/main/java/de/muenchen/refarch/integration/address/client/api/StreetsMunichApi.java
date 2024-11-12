package de.muenchen.refarch.integration.address.client.api;

import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;

/**
 * This interface defines the methods used for the communication with the address service.
 */
public interface StreetsMunichApi {

    /**
     * This method finds a street by id in the address service.
     *
     * @param streetId
     * @return
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     * @throws AddressServiceIntegrationClientErrorException
     */
    Strasse findStreetsById(long streetId)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException;

    /**
     * This method lists streets in the address service.
     *
     * @param listStreetsModel
     * @return
     * @throws AddressServiceIntegrationServerErrorException
     * @throws AddressServiceIntegrationException
     * @throws AddressServiceIntegrationClientErrorException
     */
    StrasseResponse listStreets(ListStreetsModel listStreetsModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException;

}
