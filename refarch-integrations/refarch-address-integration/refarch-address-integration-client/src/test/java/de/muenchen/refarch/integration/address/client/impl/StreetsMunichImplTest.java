package de.muenchen.refarch.integration.address.client.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationClientErrorException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationException;
import de.muenchen.refarch.integration.address.client.exception.AddressServiceIntegrationServerErrorException;
import de.muenchen.refarch.integration.address.client.impl.StreetsMunichImpl;
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
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

class StreetsMunichImplTest {

    private final StraenMnchenApi straessenMuenchenApi = Mockito.mock(StraenMnchenApi.class);
    private final StreetsMunichApi streetsMunich = new StreetsMunichImpl(straessenMuenchenApi);

    private final long streetId = 123;
    private final Strasse strasse = new Strasse();

    @BeforeEach
    void setup() {
        // basic street info
        strasse.setStrasseId(streetId);
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
    void testFindStreetsById_Success()
            throws AddressServiceIntegrationServerErrorException, AddressServiceIntegrationException, AddressServiceIntegrationClientErrorException {
        final Strasse expectedStrasse = this.strasse;
        expectedStrasse.setStrasseId(this.streetId);
        when(straessenMuenchenApi.findStrasseByNummer(streetId)).thenReturn(Mono.just(strasse));
        final Strasse result = streetsMunich.findStreetsById(streetId);
        assertThat(result).isEqualTo(expectedStrasse);
    }

    @Test
    void testFindStreetsById_ClientErrorException() {
        when(straessenMuenchenApi.findStrasseByNummer(streetId)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "message"));
        assertThatThrownBy(() -> streetsMunich.findStreetsById(streetId)).isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testFindStreetsById_ServerErrorException() {
        when(straessenMuenchenApi.findStrasseByNummer(streetId)).thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST, "message"));
        assertThatThrownBy(() -> streetsMunich.findStreetsById(streetId)).isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testFindStreetsById_RestClientException() {
        when(straessenMuenchenApi.findStrasseByNummer(streetId)).thenThrow(new RestClientException(""));
        assertThatThrownBy(() -> streetsMunich.findStreetsById(streetId)).isInstanceOf(AddressServiceIntegrationException.class);
    }

    @Test
    void testListStreets_Success()
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
    void testListStreets_ClientErrorException() {
        ListStreetsModel listStreetsModel = ListStreetsModel.builder().build(); // Create a valid model
        when(straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "message"));

        assertThatThrownBy(() -> streetsMunich.listStreets(listStreetsModel)).isInstanceOf(AddressServiceIntegrationClientErrorException.class);
    }

    @Test
    void testListStreets_ServerErrorException() {
        ListStreetsModel listStreetsModel = ListStreetsModel.builder().build(); // Create a valid model
        when(straessenMuenchenApi.listStrassen(
                listStreetsModel.getCityDistrictNames(),
                listStreetsModel.getCityDistrictNumbers(),
                listStreetsModel.getStreetName(),
                listStreetsModel.getSortdir(),
                listStreetsModel.getPage(),
                listStreetsModel.getPagesize())).thenThrow(new HttpServerErrorException(HttpStatus.BAD_REQUEST, "message"));

        assertThatThrownBy(() -> streetsMunich.listStreets(listStreetsModel)).isInstanceOf(AddressServiceIntegrationServerErrorException.class);
    }

    @Test
    void testListStreets_RestClientException() {
        ListStreetsModel listStreetsModel = ListStreetsModel.builder().build(); // Create a valid model
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
