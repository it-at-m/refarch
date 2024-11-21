package de.muenchen.refarch.integration.address.client.impl;

import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.api.StraenMnchenApi;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class StreetsMunichImpl implements StreetsMunichApi {

    private final StraenMnchenApi straessenMuenchenApi;

    @Override
    public Strasse findStreetsById(final long streetId)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.straessenMuenchenApi.findStrasseByNummer(streetId).block(), "street");
    }

    @Override
    public StrasseResponse listStreets(final ListStreetsModel listStreetsModel)
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        return ExceptionHandler.executeWithErrorHandling(() -> this.straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize()).block(), "street");
    }
}
