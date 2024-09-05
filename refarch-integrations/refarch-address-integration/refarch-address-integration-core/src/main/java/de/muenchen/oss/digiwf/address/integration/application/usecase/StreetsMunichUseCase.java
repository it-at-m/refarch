package de.muenchen.oss.digiwf.address.integration.application.usecase;

import de.muenchen.oss.digiwf.address.integration.application.port.in.StreetsMunichInPort;
import de.muenchen.oss.digiwf.address.integration.application.port.out.AddressClientOutPort;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreetsMunichUseCase implements StreetsMunichInPort {

    private final AddressClientOutPort addressClientOutPort;

    @Override
    public Strasse findStreetsById(long streetId) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.findStreetsById(streetId);
    }

    @Override
    public StrasseResponse listStreets(ListStreetsModel listStreetsModel) throws AddressServiceIntegrationException {
        return this.addressClientOutPort.listStreets(listStreetsModel);
    }
}
