package de.muenchen.refarch.integration.address.application.port.in;

import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;

/**
 * Port to integration infrastructure.
 */
public interface StreetsMunichInPort {

    /**
     * List streets in Munich.
     */
    Strasse findStreetsById(long streetId) throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    StrasseResponse listStreets(ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException;

}
