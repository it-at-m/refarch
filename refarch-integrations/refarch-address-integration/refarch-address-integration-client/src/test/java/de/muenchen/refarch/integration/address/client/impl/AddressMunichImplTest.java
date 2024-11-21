package de.muenchen.refarch.integration.address.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.client.api.AddressMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.CheckAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressChangesModel;
import de.muenchen.refarch.integration.address.client.model.request.ListAddressesModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGeoModel;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesModel;
import de.muenchen.refarch.integration.address.client.model.response.AddressDistancesModel;
import de.muenchen.refarch.integration.address.client.gen.api.AdressenMnchenApi;
import de.muenchen.refarch.integration.address.client.gen.model.AddressServicePage;
import de.muenchen.refarch.integration.address.client.gen.model.AdresseDistanz;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungResponse;
import de.muenchen.refarch.integration.address.client.gen.model.AenderungsResponseItem;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.MuenchenAdresseResponseItem;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AddressMunichImplTest {

    public static final String BAD_REQUEST = "Bad Request";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String REST_EXCEPTION = "REST exception";
    public static final String SORT = "name";
    private final AdressenMnchenApi adressenMuenchenApi = mock(AdressenMnchenApi.class);
    private final AddressMunichApi addressMunich = new AddressesMunichImpl(adressenMuenchenApi);

    private final CheckAddressesModel checkAddressesModel = this.createCheckAddressesModel();

    private final ListAddressesModel listAddressesModel = this.createListAddressesModel();
    private final ListAddressChangesModel listAddressChangesModel = this.createListAddressChangesModel();
    private final SearchAddressesModel searchAddressesModel = this.createSearchAddressModel();
    private final SearchAddressesGeoModel searchAddressesGeoModel = this.createSearchAddressesGeoModel();

    private final MuenchenAdresse muenchenAdresse = new MuenchenAdresse();
    private final MuenchenAdresseResponse muenchenAdresseResponse = new MuenchenAdresseResponse();
    private final AenderungResponse aenderungResponse = new AenderungResponse();
    private final AdresseDistanz addressDistance = new AdresseDistanz();

    @BeforeEach
    void setup() {
        muenchenAdresse.setAdresse(checkAddressesModel.getAddress());
        muenchenAdresse.setAdressId("Sample AdressId");
        muenchenAdresse.setBuchstabe("Sample Buchstabe");
        muenchenAdresse.setEhemaligeAdresse("Sample EhemaligeAdresse");
        muenchenAdresse.setHausnummer(Long.valueOf(checkAddressesModel.getHouseNumber()));
        muenchenAdresse.setOrtsname(checkAddressesModel.getCityName());
        muenchenAdresse.setStrasseId("Sample StrasseId");
        muenchenAdresse.setStrassenname(checkAddressesModel.getStreetName());
        muenchenAdresse.setStrassennameKurz("Sample StrassennameKurz");
        muenchenAdresse.setStrassennameAbgekuerzt("Sample StrassennameAbgekuerzt");

        final MuenchenAdresseResponseItem addressResponseItem = new MuenchenAdresseResponseItem();
        addressResponseItem.setAdresse(muenchenAdresse);
        muenchenAdresseResponse.setContent(List.of(addressResponseItem));
        muenchenAdresseResponse.setPage(new AddressServicePage());

        final AenderungsResponseItem aenderungsResponseItem = new AenderungsResponseItem();
        aenderungsResponseItem.setAdresse(muenchenAdresse);
        aenderungsResponseItem.setAdresseVorgaenger(muenchenAdresse);
        aenderungResponse.setContent(List.of(aenderungsResponseItem));
        aenderungResponse.setPage(new AddressServicePage());

        addressDistance.setAdresse(muenchenAdresse);
        addressDistance.setDistanz(1.0);
    }

    @Test
    void testCheckAddressSuccess()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(muenchenAdresse));
        final MuenchenAdresse result = addressMunich.checkAddress(checkAddressesModel);
        assertThat(result).isEqualTo(muenchenAdresse);
    }

    @Test
    void testCheckAddressClientErrorException() {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "message"));
        assertThatThrownBy(() -> addressMunich.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testCheckAddressServerErrorException() {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "message"));
        assertThatThrownBy(() -> addressMunich.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testCheckAddressRestClientException() {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(new RestClientException("message"));
        assertThatThrownBy(() -> addressMunich.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListAddressesSuccess()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any()))
                        .thenReturn(Mono.just(muenchenAdresseResponse));
        final MuenchenAdresseResponse result = addressMunich.listAddresses(listAddressesModel);
        assertThat(result).isEqualTo(muenchenAdresseResponse);
    }

    @Test
    void testListAddressesClientErrorException() {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any()))
                        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, BAD_REQUEST));
        assertThatThrownBy(() -> addressMunich.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testListAddressesServerErrorException() {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any()))
                        .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> addressMunich.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testListAddressesRestClientException() {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any()))
                        .thenThrow(new RestClientException(REST_EXCEPTION));
        assertThatThrownBy(() -> addressMunich.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListChangesSuccess() throws Exception {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(aenderungResponse));
        final AenderungResponse result = addressMunich.listChanges(listAddressChangesModel);
        assertThat(result).isEqualTo(aenderungResponse);
    }

    @Test
    void testListChangesClientErrorException() {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, BAD_REQUEST));
        assertThatThrownBy(() -> addressMunich.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testListChangesServerErrorException() {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> addressMunich.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testListChangesRestClientException() {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException(REST_EXCEPTION));
        assertThatThrownBy(() -> addressMunich.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesSuccess() throws Exception {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(muenchenAdresseResponse));
        final MuenchenAdresseResponse result = addressMunich.searchAddresses(searchAddressesModel);
        assertThat(result).isEqualTo(muenchenAdresseResponse);
    }

    @Test
    void testSearchAddressesClientErrorException() {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, BAD_REQUEST));
        assertThatThrownBy(() -> addressMunich.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testSearchAddressesServerErrorException() {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> addressMunich.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testSearchAddressesRestClientException() {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException(REST_EXCEPTION));
        assertThatThrownBy(() -> addressMunich.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddressesGeoSuccess() throws Exception {
        final AddressDistancesModel expectedResponse = AddressDistancesModel
                .builder()
                .addressDistances(List.of(addressDistance))
                .build();
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(addressDistance));
        final AddressDistancesModel result = addressMunich.searchAddressesGeo(searchAddressesGeoModel);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void testSearchAddressesGeoClientErrorException() {
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, BAD_REQUEST));
        assertThatThrownBy(() -> addressMunich.searchAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testSearchAddressesGeoServerErrorException() {
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR));
        assertThatThrownBy(() -> addressMunich.searchAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testSearchAddressesGeoRestClientException() {
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException(REST_EXCEPTION));
        assertThatThrownBy(() -> addressMunich.searchAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    private ListAddressesModel createListAddressesModel() {
        return ListAddressesModel.builder()
                .baublock(Collections.singletonList("Sample Baublock"))
                .erhaltungssatzung(Collections.singletonList("Sample Erhaltungssatzung"))
                .gemarkung(Collections.singletonList("Sample Gemarkung"))
                .kaminkehrerbezirk(Collections.singletonList("Sample Kaminkehrerbezirk"))
                .zip(Collections.singletonList("Sample Zip"))
                .mittelschule(Collections.singletonList("Sample Mittelschule"))
                .grundschule(Collections.singletonList("Sample Grundschule"))
                .polizeiinspektion(Collections.singletonList("Sample Polizeiinspektion"))
                .stimmbezirk(Collections.singletonList(1L))
                .stimmkreis(Collections.singletonList(1L))
                .wahlbezirk(Collections.singletonList(1L))
                .wahlkreis(Collections.singletonList(1L))
                .stadtbezirk(Collections.singletonList("Sample Stadtbezirk"))
                .stadtbezirksteil(Collections.singletonList("Sample Stadtbezirksteil"))
                .stadtbezirksviertel(Collections.singletonList("Sample Stadtbezirksviertel"))
                .sort(SORT)
                .sortdir("asc")
                .page(1)
                .pagesize(10)
                .build();
    }

    private CheckAddressesModel createCheckAddressesModel() {
        return CheckAddressesModel.builder()
                .address("Sample Address")
                .streetName("Sample Street")
                .houseNumber("123")
                .additionalInfo("Sample Additional Info")
                .zip("12345")
                .cityName("Sample City")
                .gemeindeschluessel("123456")
                .build();
    }

    private ListAddressChangesModel createListAddressChangesModel() {
        return ListAddressChangesModel.builder()
                .effectiveDateFrom("2020-01-01")
                .effectiveDateTo("2020-01-01")
                .streetName("Sample Street")
                .houseNumber(123L)
                .zip("12345")
                .additionalInfo("Sample Additional Info")
                .sorting(SORT)
                .sortingDir("asc")
                .pageNumber(1)
                .pageSize(10)
                .build();
    }

    private SearchAddressesModel createSearchAddressModel() {
        return SearchAddressesModel.builder()
                .query("Sample Query")
                .zipFilter(List.of("12345"))
                .houseNumberFilter(List.of(1L, 2L, 3L))
                .letterFilter(List.of("A", "B", "C"))
                .searchtype(SORT)
                .sort(SORT)
                .sortdir("asc")
                .page(1)
                .pagesize(10)
                .build();
    }

    private SearchAddressesGeoModel createSearchAddressesGeoModel() {
        return SearchAddressesGeoModel.builder()
                .geometry("Sample Geometry")
                .lat(1.0)
                .lng(1.0)
                .distance(1.0)
                .topleftlat(1.0)
                .topleftlng(1.0)
                .bottomrightlat(1.0)
                .bottomrightlng(1.0)
                .format("Sample Format")
                .build();
    }

}
