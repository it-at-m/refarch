package de.muenchen.refarch.integration.address.adapter.out.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.application.port.out.AddressOutPort;
import de.muenchen.refarch.integration.address.client.api.AddressGermanyApi;
import de.muenchen.refarch.integration.address.client.api.AddressMunichApi;
import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import org.junit.jupiter.api.Test;

class AddressClientOutAdapterTest {

    public static final String SOME_ERROR = "Some Error";
    public static final AddressServiceIntegrationServerErrorException ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION = new AddressServiceIntegrationServerErrorException(
            SOME_ERROR, new Exception());

    private final AddressGermanyApi addressGermanyApi = mock(AddressGermanyApi.class);
    private final AddressMunichApi addressMunichApi = mock(AddressMunichApi.class);
    private final StreetsMunichApi streetsMunichApi = mock(StreetsMunichApi.class);

    private final AddressOutPort addressOutPort = new AddressClientOutAdapter(addressGermanyApi, addressMunichApi, streetsMunichApi);

    @Test
    void testSearchGermanyAddresses()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final SearchAddressesGermanyModel model = SearchAddressesGermanyModel.builder().build();
        final BundesweiteAdresseResponse expectedResponse = new BundesweiteAdresseResponse();

        when(addressGermanyApi.searchAddresses(model)).thenReturn(expectedResponse);

        final BundesweiteAdresseResponse actualResponse = addressOutPort.searchGermanyAddresses(model);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressGermanyApi).searchAddresses(model);
    }

    @Test
    void testSearchGermanyAddresses_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final SearchAddressesGermanyModel model = SearchAddressesGermanyModel.builder().build();

        when(addressGermanyApi.searchAddresses(model)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.searchGermanyAddresses(model))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testCheckMunichAddress()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final CheckAddressesModel checkAddressesModel = CheckAddressesModel.builder().build();
        final MuenchenAdresse expectedResponse = new MuenchenAdresse();

        when(addressMunichApi.checkAddress(checkAddressesModel)).thenReturn(expectedResponse);

        final MuenchenAdresse actualResponse = addressOutPort.checkMunichAddress(checkAddressesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressMunichApi).checkAddress(checkAddressesModel);
    }

    @Test
    void testCheckMunichAddress_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final CheckAddressesModel checkAddressesModel = CheckAddressesModel.builder().build();

        when(addressMunichApi.checkAddress(checkAddressesModel)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.checkMunichAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testListMunichAddresses()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final ListAddressesModel listAddressesModel = ListAddressesModel.builder().build();
        final MuenchenAdresseResponse expectedResponse = new MuenchenAdresseResponse();

        when(addressMunichApi.listAddresses(listAddressesModel)).thenReturn(expectedResponse);

        final MuenchenAdresseResponse actualResponse = addressOutPort.listMunichAddresses(listAddressesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressMunichApi).listAddresses(listAddressesModel);
    }

    @Test
    void testListMunichAddresses_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final ListAddressesModel listAddressesModel = ListAddressesModel.builder().build();

        when(addressMunichApi.listAddresses(listAddressesModel)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.listMunichAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testListMunichChanges()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final ListAddressChangesModel listAddressChangesModel = ListAddressChangesModel.builder().build();
        final AenderungResponse expectedResponse = new AenderungResponse();

        when(addressMunichApi.listChanges(listAddressChangesModel)).thenReturn(expectedResponse);

        final AenderungResponse actualResponse = addressOutPort.listMunichChanges(listAddressChangesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressMunichApi).listChanges(listAddressChangesModel);
    }

    @Test
    void testListMunichChanges_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final ListAddressChangesModel listAddressChangesModel = ListAddressChangesModel.builder().build();

        when(addressMunichApi.listChanges(listAddressChangesModel)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.listMunichChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testSearchMunichAddresses()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final SearchAddressesModel searchAddressesModel = SearchAddressesModel.builder().build();
        final MuenchenAdresseResponse expectedResponse = new MuenchenAdresseResponse();

        when(addressMunichApi.searchAddresses(searchAddressesModel)).thenReturn(expectedResponse);

        final MuenchenAdresseResponse actualResponse = addressOutPort.searchMunichAddresses(searchAddressesModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressMunichApi).searchAddresses(searchAddressesModel);
    }

    @Test
    void testSearchMunichAddresses_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final SearchAddressesModel searchAddressesModel = SearchAddressesModel.builder().build();

        when(addressMunichApi.searchAddresses(searchAddressesModel)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.searchMunichAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testSearchMunichAddressesGeo()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final SearchAddressesGeoModel searchAddressesGeoModel = SearchAddressesGeoModel.builder().build();
        final AddressDistancesModel expectedResponse = AddressDistancesModel.builder().build();

        when(addressMunichApi.searchAddressesGeo(searchAddressesGeoModel)).thenReturn(expectedResponse);

        final AddressDistancesModel actualResponse = addressOutPort.searchMunichAddressesGeo(searchAddressesGeoModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(addressMunichApi).searchAddressesGeo(searchAddressesGeoModel);
    }

    @Test
    void testSearchMunichAddressesGeo_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final SearchAddressesGeoModel searchAddressesGeoModel = SearchAddressesGeoModel.builder().build();

        when(addressMunichApi.searchAddressesGeo(searchAddressesGeoModel)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.searchMunichAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testFindMunichStreetsById()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final long streetId = 0L;
        final Strasse expectedResponse = new Strasse();

        when(streetsMunichApi.findStreetsById(streetId)).thenReturn(expectedResponse);

        final Strasse actualResponse = addressOutPort.findMunichStreetsById(streetId);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(streetsMunichApi).findStreetsById(streetId);
    }

    @Test
    void testFindMunichStreetsById_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final long streetId = 0L;

        when(streetsMunichApi.findStreetsById(streetId)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.findMunichStreetsById(streetId))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }

    @Test
    void testListMunichStreets()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build();
        final StrasseResponse expectedResponse = new StrasseResponse();

        when(streetsMunichApi.listStreets(listStreetsModel)).thenReturn(expectedResponse);

        final StrasseResponse actualResponse = addressOutPort.listMunichStreets(listStreetsModel);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(streetsMunichApi).listStreets(listStreetsModel);
    }

    @Test
    void testListMunichStreets_throwsAddressServiceIntegrationException()
            throws AddressServiceIntegrationException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationClientErrorException {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build();

        when(streetsMunichApi.listStreets(listStreetsModel)).thenThrow(ADDRESS_SERVICE_INTEGRATION_SERVER_ERROR_EXCEPTION);

        assertThatThrownBy(() -> addressOutPort.listMunichStreets(listStreetsModel))
                .isInstanceOf(AddressServiceIntegrationException.class)
                .hasMessage(SOME_ERROR);
    }
}
