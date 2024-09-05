package de.muenchen.refarch.integration.address.adapter.out.address;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import de.muenchen.refarch.integration.address.adapter.out.address.AddressClientOutAdapter;
import de.muenchen.refarch.integration.address.application.port.out.AddressClientOutPort;
import de.muenchen.refarch.integration.address.client.api.AddressGermanyApi;
import de.muenchen.refarch.integration.address.client.api.AddressMunichApi;
import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AddressClientOutAdapterTest {

    private final AddressGermanyApi addressGermanyApi = mock(AddressGermanyApi.class);
    private final AddressMunichApi addressMunichApi = mock(AddressMunichApi.class);
    private final StreetsMunichApi streetsMunichApi = mock(StreetsMunichApi.class);

    private final AddressClientOutPort addressClientOutPort = new AddressClientOutAdapter(addressGermanyApi, addressMunichApi, streetsMunichApi);

    @AfterEach
    void setup() {
        reset(addressGermanyApi);
        reset(addressMunichApi);
        reset(streetsMunichApi);
    }

    @Test
    void testSearchAddressesThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(addressGermanyApi)
                .searchAddresses(any(SearchAddressesGermanyModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddresses(SearchAddressesGermanyModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(addressGermanyApi)
                .searchAddresses(any(SearchAddressesGermanyModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddresses(SearchAddressesGermanyModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(addressGermanyApi)
                .searchAddresses(any(SearchAddressesGermanyModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddresses(SearchAddressesGermanyModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testCheckAddressThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(addressMunichApi)
                .checkAddress(any(CheckAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.checkAddress(CheckAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testCheckAddressThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(addressMunichApi)
                .checkAddress(any(CheckAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.checkAddress(CheckAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testCheckAddressThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(addressMunichApi).checkAddress(any(CheckAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.checkAddress(CheckAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListAddressesThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(addressMunichApi)
                .listAddresses(any(ListAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listAddresses(ListAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListAddressesThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(addressMunichApi)
                .listAddresses(any(ListAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listAddresses(ListAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListAddressesThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(addressMunichApi).listAddresses(any(ListAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listAddresses(ListAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListChangesThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(addressMunichApi)
                .listChanges(any(ListAddressChangesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listChanges(ListAddressChangesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListChangesThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(addressMunichApi)
                .listChanges(any(ListAddressChangesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listChanges(ListAddressChangesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListChangesThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(addressMunichApi).listChanges(any(ListAddressChangesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listChanges(ListAddressChangesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesMunichThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(addressMunichApi)
                .searchAddresses(any(SearchAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddresses(SearchAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesMunichThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(addressMunichApi)
                .searchAddresses(any(SearchAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddresses(SearchAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesMunichThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(addressMunichApi).searchAddresses(any(SearchAddressesModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddresses(SearchAddressesModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesGeoThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(addressMunichApi)
                .searchAddressesGeo(any(SearchAddressesGeoModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddressesGeo(SearchAddressesGeoModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesGeoThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(addressMunichApi)
                .searchAddressesGeo(any(SearchAddressesGeoModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddressesGeo(SearchAddressesGeoModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesGeoThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(addressMunichApi)
                .searchAddressesGeo(any(SearchAddressesGeoModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.searchAddressesGeo(SearchAddressesGeoModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchStreetsThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(streetsMunichApi).findStreetsById(anyLong());
        assertThatThrownBy(() -> this.addressClientOutPort.findStreetsById(1L))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchStreetsThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(streetsMunichApi).findStreetsById(anyLong());
        assertThatThrownBy(() -> this.addressClientOutPort.findStreetsById(1L))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchStreetsThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(streetsMunichApi).findStreetsById(anyLong());
        assertThatThrownBy(() -> this.addressClientOutPort.findStreetsById(1L))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListStreetsThrowsAddressServiceIntegrationException()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationClientErrorException("test", new RuntimeException())).when(streetsMunichApi)
                .listStreets(any(ListStreetsModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listStreets(ListStreetsModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListStreetsThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationServerErrorExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationServerErrorException("test", new RuntimeException())).when(streetsMunichApi)
                .listStreets(any(ListStreetsModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listStreets(ListStreetsModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListStreetsThrowsAddressServiceIntegrationExceptionIfAddressServiceIntegrationExceptionOccurs()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        doThrow(new AddressServiceIntegrationException("test", new RuntimeException())).when(streetsMunichApi).listStreets(any(ListStreetsModel.class));
        assertThatThrownBy(() -> this.addressClientOutPort.listStreets(ListStreetsModel.builder().build()))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }
}
