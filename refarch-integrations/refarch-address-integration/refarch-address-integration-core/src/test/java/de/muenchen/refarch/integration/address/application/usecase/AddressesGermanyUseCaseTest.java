package de.muenchen.refarch.integration.address.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.application.port.in.AddressGermanyInPort;
import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import org.junit.jupiter.api.Test;

class AddressesGermanyUseCaseTest {

    private final AddressClientOutPort addressClientOutPort = mock(AddressClientOutPort.class);

    private final AddressGermanyInPort addressesGermanyUseCase = new AddressesGermanyUseCase(addressClientOutPort);

    @Test
    void testSearchAddressesReturnsBundesweiteAdresseResponse() throws AddressServiceIntegrationException {
        final SearchAddressesGermanyModel model = SearchAddressesGermanyModel.builder().build();
        final BundesweiteAdresseResponse expectedResponse = new BundesweiteAdresseResponse();

        when(addressClientOutPort.searchAddresses(model)).thenReturn(expectedResponse);

        final BundesweiteAdresseResponse actualResponse = addressesGermanyUseCase.searchAddresses(model);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).searchAddresses(model);
    }

    @Test
    void testSearchAddressesThrowsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final SearchAddressesGermanyModel model = SearchAddressesGermanyModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException("SomeError", new Exception("SomeError"));

        when(addressClientOutPort.searchAddresses(model)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesGermanyUseCase.searchAddresses(model))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }
}
