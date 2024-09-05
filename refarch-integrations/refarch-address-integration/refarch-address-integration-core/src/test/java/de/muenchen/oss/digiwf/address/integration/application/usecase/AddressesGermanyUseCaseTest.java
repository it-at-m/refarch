package de.muenchen.oss.digiwf.address.integration.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.digiwf.address.integration.application.port.in.AddressGermanyInPort;
import de.muenchen.oss.digiwf.address.integration.application.port.out.AddressClientOutPort;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AddressesGermanyUseCaseTest {

    private final AddressClientOutPort addressClientOutPort = Mockito.mock(AddressClientOutPort.class);

    private final AddressGermanyInPort addressesGermanyUseCase = new AddressesGermanyUseCase(addressClientOutPort);

    @Test
    void testSearchAddresses_returnsBundesweiteAdresseResponse() throws AddressServiceIntegrationException {
        SearchAddressesGermanyModel model = SearchAddressesGermanyModel.builder().build();
        BundesweiteAdresseResponse expectedResponse = new BundesweiteAdresseResponse();

        when(addressClientOutPort.searchAddresses(model)).thenReturn(expectedResponse);

        BundesweiteAdresseResponse actualResponse = addressesGermanyUseCase.searchAddresses(model);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressClientOutPort).searchAddresses(model);
    }

    @Test
    void testSearchAddresses_throwsAddressServiceIntegrationException() throws AddressServiceIntegrationException {
        final SearchAddressesGermanyModel model = SearchAddressesGermanyModel.builder().build();
        final AddressServiceIntegrationException expectedError = new AddressServiceIntegrationException("SomeError", new Exception("SomeError"));

        when(addressClientOutPort.searchAddresses(model)).thenThrow(expectedError);

        assertThatThrownBy(() -> addressesGermanyUseCase.searchAddresses(model))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .isEqualTo(expectedError);
    }
}
