package de.muenchen.refarch.integration.address.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.client.api.AddressGermanyApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.gen.api.AdressenBundesweitApi;
import de.muenchen.refarch.integration.address.client.gen.model.AddressServicePage;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresse;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.BundesweiteAdresseResponseItem;
import de.muenchen.refarch.integration.address.client.model.request.SearchAddressesGermanyModel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

class AddressGermanyImplTest {

    public static final String QUERY = "Sample Query";
    public static final String ZIP = "12345";
    public static final String CITY = "Sample City";
    public static final String GEMEINDESCHLUESSEL = "456789";
    public static final String SORT = "name";
    public static final String SORTDIR = "asc";
    private final AdressenBundesweitApi adressenBundesweitApi = mock(AdressenBundesweitApi.class);
    private final AddressGermanyApi addressGermany = new AddressGermanyImpl(adressenBundesweitApi);

    private final SearchAddressesGermanyModel searchAddressesModel = SearchAddressesGermanyModel.builder().build();
    private final BundesweiteAdresseResponse bundesweiteAdresseResponse = new BundesweiteAdresseResponse();

    @BeforeEach
    void setup() {
        this.searchAddressesModel.setQuery(QUERY);
        this.searchAddressesModel.setZip(ZIP);
        this.searchAddressesModel.setCityName(CITY);
        this.searchAddressesModel.setGemeindeschluessel(GEMEINDESCHLUESSEL);
        this.searchAddressesModel.setHouseNumberFilter(List.of(1L, 2L, 3L));
        this.searchAddressesModel.setLetterFilter(List.of("A", "B", "C"));
        this.searchAddressesModel.setSort(SORT);
        this.searchAddressesModel.setSortdir(SORTDIR);
        this.searchAddressesModel.setPage(1);
        this.searchAddressesModel.setPagesize(10);

        bundesweiteAdresseResponse.page(new AddressServicePage());
        final BundesweiteAdresseResponseItem bundesweiteAdresseResponseItem = new BundesweiteAdresseResponseItem();
        final BundesweiteAdresse bundesweiteAdresse = new BundesweiteAdresse();
        bundesweiteAdresse.setStrassenname("streetName");
        bundesweiteAdresse.setHausnummer(1L);
        bundesweiteAdresse.setOrtsname("cityName");
        bundesweiteAdresseResponseItem.setAdresse(bundesweiteAdresse);
        bundesweiteAdresseResponse.setContent(List.of(bundesweiteAdresseResponseItem));
    }

    @Test
    void testFindStreetsByIdSuccess()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        when(this.adressenBundesweitApi.searchAdressen(
                QUERY,
                ZIP,
                CITY,
                GEMEINDESCHLUESSEL,
                List.of(1L, 2L, 3L),
                List.of("A", "B", "C"),
                SORT,
                SORTDIR,
                1,
                10)).thenReturn(Mono.just(bundesweiteAdresseResponse));
        final BundesweiteAdresseResponse result = addressGermany.searchAddresses(this.searchAddressesModel);
        assertThat(result).isEqualTo(bundesweiteAdresseResponse);
    }

    @Test
    void testFindStreetsByIdClientErrorException() {
        when(this.adressenBundesweitApi.searchAdressen(
                QUERY,
                ZIP,
                CITY,
                GEMEINDESCHLUESSEL,
                List.of(1L, 2L, 3L),
                List.of("A", "B", "C"),
                SORT,
                SORTDIR,
                1,
                10)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "message"));
        assertThatThrownBy(() -> addressGermany.searchAddresses(this.searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testFindStreetsByIdServerErrorException() {
        when(this.adressenBundesweitApi.searchAdressen(
                QUERY,
                ZIP,
                CITY,
                GEMEINDESCHLUESSEL,
                List.of(1L, 2L, 3L),
                List.of("A", "B", "C"),
                SORT,
                SORTDIR,
                1,
                10)).thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST, "message"));
        assertThatThrownBy(() -> addressGermany.searchAddresses(this.searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testFindStreetsByIdRestClientException() {
        when(this.adressenBundesweitApi.searchAdressen(
                QUERY,
                ZIP,
                CITY,
                GEMEINDESCHLUESSEL,
                List.of(1L, 2L, 3L),
                List.of("A", "B", "C"),
                SORT,
                SORTDIR,
                1,
                10)).thenThrow(new RestClientException(""));
        assertThatThrownBy(() -> addressGermany.searchAddresses(this.searchAddressesModel))
                .isInstanceOf(AddressServiceIntegrationException.class);
    }
}
