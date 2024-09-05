package de.muenchen.oss.digiwf.address.integration.application.port.in;

import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;

/**
 * Port to integration infrastructure.
 */
public interface StreetsMunichInPort {

    /**
     * List streets in Munich.
     */
    Strasse findStreetsById(final long streetId) throws AddressServiceIntegrationException;

    /**
     * List streets in Munich.
     */
    StrasseResponse listStreets(final ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException;

}
