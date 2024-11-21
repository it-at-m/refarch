package de.muenchen.refarch.integration.address.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.application.port.in.AddressMunichInPort;
import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import org.junit.jupiter.api.Test;

class AddressesMunichUseCaseTest {

    public static final String SOME_ERROR = "SomeError";
    public static final String MESSAGE = "400";
    private final AddressClientOutPort addressClientOutPort = mock(AddressClientOutPort.class);

    private final AddressMunichInPort addressesMunichUseCase = new AddressesMunichUseCase(addressClientOutPort);

    @Test
    void testCheckAddressReturnsMuenchenAdresse() throws AddressServiceIntegrationException {
        final CheckAddressesModel checkAddressesModel = CheckAddressesModel.builder().build();
        final MuenchenAdresse expectedResponse = new MuenchenAdresse();

        when(addressClientOutPort.checkAddress(checkAddressesModel)).thenReturn(expectedResponse);

        final MuenchenAdresse actualResponse = addressesMunichUseCase.checkAddress(checkAddressesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).checkAddress(checkAddressesModel);
    }

    @Test
    void testCheckAddressThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final CheckAddressesModel checkAddressesModel = CheckAddressesModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE, new Exception(SOME_ERROR));

        when(addressClientOutPort.checkAddress(checkAddressesModel)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesMunichUseCase.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

    @Test
    void testListAddressesReturnsMuenchenAdresseResponse() throws AddressServiceIntegrationException {
        final ListAddressesModel listAddressesModel = ListAddressesModel.builder().build();
        final MuenchenAdresseResponse expectedResponse = new MuenchenAdresseResponse();

        when(addressClientOutPort.listAddresses(listAddressesModel)).thenReturn(expectedResponse);

        final MuenchenAdresseResponse actualResponse = addressesMunichUseCase.listAddresses(listAddressesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).listAddresses(listAddressesModel);
    }

    @Test
    void testListAddressesThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final ListAddressesModel listAddressesModel = ListAddressesModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE, new Exception(SOME_ERROR));

        when(addressClientOutPort.listAddresses(listAddressesModel)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesMunichUseCase.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

    @Test
    void testListChangesReturnsAenderungResponse() throws AddressServiceIntegrationException {
        final ListAddressChangesModel listAddressChangesModel = ListAddressChangesModel.builder().build();
        final AenderungResponse expectedResponse = new AenderungResponse();

        when(addressClientOutPort.listChanges(listAddressChangesModel)).thenReturn(expectedResponse);

        final AenderungResponse actualResponse = addressesMunichUseCase.listChanges(listAddressChangesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).listChanges(listAddressChangesModel);
    }

    @Test
    void testListChangesThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final ListAddressChangesModel listAddressChangesModel = ListAddressChangesModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE, new Exception(SOME_ERROR));

        when(addressClientOutPort.listChanges(listAddressChangesModel)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesMunichUseCase.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

    @Test
    void testSearchAddressesReturnsMuenchenAdresseResponse() throws AddressServiceIntegrationException {
        final SearchAddressesModel searchAddressesModel = SearchAddressesModel.builder().build();
        final MuenchenAdresseResponse expectedResponse = new MuenchenAdresseResponse();

        when(addressClientOutPort.searchAddresses(searchAddressesModel)).thenReturn(expectedResponse);

        final MuenchenAdresseResponse actualResponse = addressesMunichUseCase.searchAddresses(searchAddressesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).searchAddresses(searchAddressesModel);
    }

    @Test
    void testSearchAddressesThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final SearchAddressesModel searchAddressesModel = SearchAddressesModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE, new Exception(SOME_ERROR));

        when(addressClientOutPort.searchAddresses(searchAddressesModel)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesMunichUseCase.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

    @Test
    void testSearchAddressesGeoReturnsAddressDistancesModel() throws AddressServiceIntegrationException {
        final SearchAddressesGeoModel searchAddressesGeoModel = SearchAddressesGeoModel.builder().build();
        final AddressDistancesModel expectedResponse = AddressDistancesModel.builder().build();

        when(addressClientOutPort.searchAddressesGeo(searchAddressesGeoModel)).thenReturn(expectedResponse);

        final AddressDistancesModel actualResponse = addressesMunichUseCase.searchAddressesGeo(searchAddressesGeoModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).searchAddressesGeo(searchAddressesGeoModel);
    }

    @Test
    void testSearchAddressesGeoThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final SearchAddressesGeoModel searchAddressesGeoModel = SearchAddressesGeoModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException(MESSAGE, new Exception(SOME_ERROR));

        when(addressClientOutPort.searchAddressesGeo(searchAddressesGeoModel)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesMunichUseCase.searchAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }

}
