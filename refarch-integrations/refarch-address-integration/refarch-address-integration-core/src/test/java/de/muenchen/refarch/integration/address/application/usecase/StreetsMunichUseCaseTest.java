package de.muenchen.refarch.integration.address.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.application.port.in.StreetsMunichInPort;
import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import org.junit.jupiter.api.Test;

class StreetsMunichUseCaseTest {

    public static final String SOME_ERROR = "SomeError";
    public static final String MESSAGE_400 = "400";
    private final AddressClientOutPort addressClientOutPort = mock(AddressClientOutPort.class);

    private final StreetsMunichInPort streetsMunichUseCase = new StreetsMunichUseCase(addressClientOutPort);

    @Test
    void testFindStreetsByIdReturnsStrasse() throws AddressServiceIntegrationException {
        final long streetId = 0L;
        final Strasse expectedResponse = new Strasse();

        when(addressClientOutPort.findStreetsById(streetId)).thenReturn(expectedResponse);

        final Strasse actualResponse = streetsMunichUseCase.findStreetsById(streetId);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).findStreetsById(streetId);
    }

    @Test
    void testFindStreetsByIdThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final long streetId = 0L;
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE_400, new Exception(SOME_ERROR));

        when(addressClientOutPort.findStreetsById(streetId)).thenThrow(expectedError);

        assertThatThrownBy(() -> streetsMunichUseCase.findStreetsById(streetId))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

    @Test
    void testListStreetsReturnsStrasseResponse() throws AddressServiceIntegrationException {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build();
        final StrasseResponse expectedResponse = new StrasseResponse();

        when(addressClientOutPort.listStreets(listStreetsModel)).thenReturn(expectedResponse);

        final StrasseResponse actualResponse = streetsMunichUseCase.listStreets(listStreetsModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).listStreets(listStreetsModel);
    }

    @Test
    void testListStreetsThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE_400, new Exception(SOME_ERROR));

        when(addressClientOutPort.listStreets(listStreetsModel)).thenThrow(expectedError);

        assertThatThrownBy(() -> streetsMunichUseCase.listStreets(listStreetsModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

}
