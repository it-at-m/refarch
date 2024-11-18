package de.muenchen.refarch.integration.address.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.model.request.ListStreetsModel;
import de.muenchen.refarch.integration.address.client.gen.api.StraenMnchenApi;
import de.muenchen.refarch.integration.address.client.gen.model.AddressServicePage;
import de.muenchen.refarch.integration.address.client.gen.model.Stadtbezirk;
import de.muenchen.refarch.integration.address.client.gen.model.Strasse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseGeozuordnungen;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponse;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseResponseItem;
import de.muenchen.refarch.integration.address.client.gen.model.StrasseVerwaltungszuteilung;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

class StreetsMunichImplTest {

    public static final String MESSAGE = "message";
    private final StraenMnchenApi straessenMuenchenApi = mock(StraenMnchenApi.class);
    private final StreetsMunichApi streetsMunich = new StreetsMunichImpl(straessenMuenchenApi);

    private static final long STREET_ID = 123;
    private final Strasse strasse = new Strasse();

    @BeforeEach
    void setup() {
        // basic street info
        strasse.setStrasseId(STREET_ID);
        strasse.setStrassenname("streetName");
        strasse.setStrassennameKurz("streetNameShort");
        strasse.setStrassennameAbgekuerzt("streetNameAbbreviated");

        // geo info
        final Stadtbezirk stadtbezirk = new Stadtbezirk();
        stadtbezirk.setNummer(1L);
        stadtbezirk.setName("Stadtbezirk 1");

        final StrasseVerwaltungszuteilung verwaltungszuteilung = new StrasseVerwaltungszuteilung();
        verwaltungszuteilung.setStadtbezirke(List.of(stadtbezirk));

        final StrasseGeozuordnungen strasseGeozuordnungen = new StrasseGeozuordnungen();
        strasseGeozuordnungen.setVerwaltungszuteilung(verwaltungszuteilung);

        strasse.setGeozuordnungen(strasseGeozuordnungen);
    }

    @Test
    void testFindStreetsByIdSuccess()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        final Strasse expectedStrasse = this.strasse;
        expectedStrasse.setStrasseId(this.STREET_ID);
        when(straessenMuenchenApi.findStrasseByNummer(STREET_ID)).thenReturn(Mono.just(strasse));
        final Strasse result = streetsMunich.findStreetsById(STREET_ID);
        assertThat(result).isEqualTo(expectedStrasse);
    }

    @Test
    void testFindStreetsByIdClientErrorException() {
        when(straessenMuenchenApi.findStrasseByNummer(STREET_ID)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, MESSAGE));
        assertThatThrownBy(() -> streetsMunich.findStreetsById(STREET_ID)).isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testFindStreetsByIdServerErrorException() {
        when(straessenMuenchenApi.findStrasseByNummer(STREET_ID)).thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST, MESSAGE));
        assertThatThrownBy(() -> streetsMunich.findStreetsById(STREET_ID)).isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testFindStreetsByIdRestClientException() {
        when(straessenMuenchenApi.findStrasseByNummer(STREET_ID)).thenThrow(new RestClientException(""));
        assertThatThrownBy(() -> streetsMunich.findStreetsById(STREET_ID)).isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListStreetsSuccess()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder()
                .streetName("streetName")
                .build();

        final StrasseResponse expectedResponse = new StrasseResponse();
        expectedResponse.setPage(new AddressServicePage());
        final StrasseResponseItem strasseResponseItem = new StrasseResponseItem();
        strasseResponseItem.setScore(1.0f);
        strasseResponseItem.setStrasse(this.strasse);
        expectedResponse.setContent(List.of(strasseResponseItem));

        when(straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize())).thenReturn(Mono.just(expectedResponse));

        final StrasseResponse result = streetsMunich.listStreets(listStreetsModel);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void testListStreetsClientErrorException() {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build(); // Create a valid model
        when(straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, MESSAGE));

        assertThatThrownBy(() -> streetsMunich.listStreets(listStreetsModel)).isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testListStreetsServerErrorException() {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build(); // Create a valid model
        when(straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize())).thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST, MESSAGE));

        assertThatThrownBy(() -> streetsMunich.listStreets(listStreetsModel)).isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testListStreetsRestClientException() {
        final ListStreetsModel listStreetsModel = ListStreetsModel.builder().build(); // Create a valid model
        when(straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize())).thenThrow(new RestClientException(""));

        assertThatThrownBy(() -> streetsMunich.listStreets(listStreetsModel)).isInstanceOf(AddressServiceIntegrationException.class);
    }

}
