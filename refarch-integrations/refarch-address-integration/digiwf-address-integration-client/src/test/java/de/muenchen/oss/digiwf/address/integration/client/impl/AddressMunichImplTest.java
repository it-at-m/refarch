package de.muenchen.oss.digiwf.address.integration.client.impl;

import de.muenchen.oss.digiwf.address.integration.client.api.AddressMunichApi;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationException;
import de.muenchen.oss.digiwf.address.integration.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.oss.digiwf.address.integration.client.gen.api.AdressenMnchenApi;
import de.muenchen.oss.digiwf.address.integration.client.gen.model.*;
import de.muenchen.oss.digiwf.address.integration.client.model.request.*;
import de.muenchen.oss.digiwf.address.integration.client.model.response.AddressDistancesModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AddressMunichImplTest {

    private final AdressenMnchenApi adressenMuenchenApi = Mockito.mock(AdressenMnchenApi.class);
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
    void testCheckAddress_Success() throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException, AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(muenchenAdresse));
        final MuenchenAdresse result = addressMunich.checkAddress(checkAddressesModel);
        assertThat(result).isEqualTo(muenchenAdresse);
    }

    @Test
    void testCheckAddress_ClientErrorException() {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "message"));
        assertThatThrownBy(() -> addressMunich.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testCheckAddress_ServerErrorException() {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "message"));
        assertThatThrownBy(() -> addressMunich.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testCheckAddress_RestClientException() {
        when(adressenMuenchenApi.checkAdresse(any(), any(), any(), any(), any(), any(), any(), any())).thenThrow(new RestClientException("message"));
        assertThatThrownBy(() -> addressMunich.checkAddress(checkAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListAddresses_Success() throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(muenchenAdresseResponse));
        final MuenchenAdresseResponse result = addressMunich.listAddresses(listAddressesModel);
        assertThat(result).isEqualTo(muenchenAdresseResponse);
    }

    @Test
    void testListAddresses_ClientErrorException() {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        assertThatThrownBy(() -> addressMunich.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testListAddresses_ServerErrorException() {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
        assertThatThrownBy(() -> addressMunich.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testListAddresses_RestClientException() {
        when(adressenMuenchenApi.listAdressen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("REST exception"));
        assertThatThrownBy(() -> addressMunich.listAddresses(listAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListChanges_Success() throws Exception {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(aenderungResponse));
        final AenderungResponse result = addressMunich.listChanges(listAddressChangesModel);
        assertThat(result).isEqualTo(aenderungResponse);
    }

    @Test
    void testListChanges_ClientErrorException() {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        assertThatThrownBy(() -> addressMunich.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testListChanges_ServerErrorException() {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
        assertThatThrownBy(() -> addressMunich.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testListChanges_RestClientException() {
        when(adressenMuenchenApi.listAenderungen(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("REST exception"));
        assertThatThrownBy(() -> addressMunich.listChanges(listAddressChangesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testSearchAddresses_Success() throws Exception {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(muenchenAdresseResponse));
        MuenchenAdresseResponse result = addressMunich.searchAddresses(searchAddressesModel);
        assertThat(result).isEqualTo(muenchenAdresseResponse);
    }

    @Test
    void testSearchAddresses_ClientErrorException() {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        assertThatThrownBy(() -> addressMunich.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testSearchAddresses_ServerErrorException() {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
        assertThatThrownBy(() -> addressMunich.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testSearchAddresses_RestClientException() {
        when(adressenMuenchenApi.searchAdressen1(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("REST exception"));
        assertThatThrownBy(() -> addressMunich.searchAddresses(searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }


    @Test
    void testSearchAddressesGeo_Success() throws Exception {
        final AddressDistancesModel expectedResponse = AddressDistancesModel
                .builder()
                .addressDistances(List.of(addressDistance))
                .build();
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Flux.just(addressDistance));
        AddressDistancesModel result = addressMunich.searchAddressesGeo(searchAddressesGeoModel);
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void testSearchAddressesGeo_ClientErrorException() {
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));
        assertThatThrownBy(() -> addressMunich.searchAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testSearchAddressesGeo_ServerErrorException() {
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
        assertThatThrownBy(() -> addressMunich.searchAddressesGeo(searchAddressesGeoModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testSearchAddressesGeo_RestClientException() {
        when(adressenMuenchenApi.searchAdressenGeo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RestClientException("REST exception"));
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
                .sort("name")
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
                .sorting("name")
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
                .searchtype("name")
                .sort("name")
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
