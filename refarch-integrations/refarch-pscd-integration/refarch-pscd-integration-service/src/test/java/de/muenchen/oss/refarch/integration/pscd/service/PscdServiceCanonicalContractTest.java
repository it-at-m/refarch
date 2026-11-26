package de.muenchen.oss.refarch.integration.pscd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.ws.test.server.RequestCreators.withPayload;

import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart010;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart100;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart105;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart155;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart165;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart200;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart210;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart250;
import de.muenchen.oss.refarch.integration.pscd.domain.model.Satzart260;
import de.muenchen.oss.refarch.integration.pscd.domain.model.SatzartFehler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

/**
 * The canonical contract, every record type and every mapped field of it, over both channels that
 * speak it.
 *
 * <p>
 * {@code PscdInboundCanonicalMapper} is nine hand-written blocks of near-identical assignments,
 * which is exactly the shape where {@code psobtxtb3} ends up in {@code psobtxtb4} and no test
 * notices. So this sends one batch carrying all nine types plus a standalone error record, with a
 * value naming its own field, and compares the delivered domain batch against the aggregate it
 * should be, field by field, by recursive comparison, so a field added to the schema and forgotten
 * in the mapper fails here too.
 * </p>
 *
 * <p>
 * The same expectation is used for JSON and for XML, which is the other half of the point: the two
 * edges share the generated model and the mapper, and must not drift apart. Asserted at
 * {@code PscdOutPort}, on the domain batch, because what is under test is the mapping onto the
 * domain, not the outbound SOAP request.
 * </p>
 */
@SpringBootTest(
        properties = {
                "refarch.pscd.inbound.rest.enabled=true",
                "refarch.pscd.inbound.soap.enabled=true",
                "refarch.pscd.inbound.file.enabled=false",
                "refarch.pscd.inbound.security.username=pscd-sender",
                "refarch.pscd.inbound.security.password=s3cret"
        }
)
@AutoConfigureMockMvc
class PscdServiceCanonicalContractTest {

    private static final String BATCHES = "/api/pscd/batches";
    private static final String FILENAME = "canonical-contract.dat";

    /** The HTTP channels always require HTTP Basic; the SOAP payload below goes in below that level. */
    private static final String BASIC_AUTH = "Basic "
            + Base64.getEncoder().encodeToString("pscd-sender:s3cret".getBytes(StandardCharsets.UTF_8));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private PscdOutPort pscdOutPort;

    @Test
    void aJsonBatchMapsEveryFieldOfEveryRecordType() throws Exception {
        this.mockMvc.perform(post(BATCHES)
                .header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload()))
                .andExpect(status().isAccepted());

        assertThat(delivered()).usingRecursiveComparison().isEqualTo(expectedBatch());
    }

    @Test
    void aSoapBatchMapsEveryFieldOfEveryRecordType() {
        MockWebServiceClient.createClient(this.applicationContext)
                .sendRequest(withPayload(new StringSource(xmlPayload())));

        assertThat(delivered()).usingRecursiveComparison().isEqualTo(expectedBatch());
    }

    private PscdSatzarten delivered() {
        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort).send(captor.capture());
        return captor.getValue();
    }

    /**
     * The aggregate both payloads describe. Every value names the record type and the field it belongs
     * to, so a mapping that crosses two fields fails with both names in the message.
     */
    private static PscdSatzarten expectedBatch() {
        return PscdSatzarten.builder()
                .filename(FILENAME)
                .satzart010(Satzart010.builder()
                        .satzart("010").abstimmsumme("S010-abstimmsumme").vorzeichen("S010-vorzeichen")
                        .fehler("S010-fehler")
                        .build())
                .satzart100(List.of(Satzart100.builder()
                        .satzart("100").psobkey("S100-psobkey").partner("S100-partner").addrnum("S100-addrnum")
                        .psobtxtb1("S100-psobtxtb1").psobtxtb2("S100-psobtxtb2").psobtxtb3("S100-psobtxtb3")
                        .psobtxtb4("S100-psobtxtb4").psobtxtb5("S100-psobtxtb5").psobtxtb6("S100-psobtxtb6")
                        .zweitschuldner("S100-zweitschuldner").eigentuemerwechsel("S100-eigentuemerwechsel")
                        .betriebsende("S100-betriebsende").kdKenn("S100-kdKenn").fachdstSb("S100-fachdstSb")
                        .fachdstTelnr("S100-fachdstTelnr").fehler("S100-fehler")
                        .build()))
                .satzart105(List.of(Satzart105.builder()
                        .satzart("105").psobkey("S105-psobkey").corrPartner("S105-corrPartner")
                        .corrRole("S105-corrRole").fehler("S105-fehler")
                        .build()))
                .satzart155(List.of(Satzart155.builder()
                        .satzart("155").psobkey("S155-psobkey").deleteFlag("S155-deleteFlag")
                        .corrRole("S155-corrRole").fehler("S155-fehler")
                        .build()))
                .satzart165(List.of(Satzart165.builder()
                        .satzart("165").psobkey("S165-psobkey")
                        .psobtxtb1("S165-psobtxtb1").psobtxtb2("S165-psobtxtb2").psobtxtb3("S165-psobtxtb3")
                        .psobtxtb4("S165-psobtxtb4").psobtxtb5("S165-psobtxtb5").psobtxtb6("S165-psobtxtb6")
                        .zweitschuldner("S165-zweitschuldner").eigentuemerwechsel("S165-eigentuemerwechsel")
                        .betriebsende("S165-betriebsende").kdKenn("S165-kdKenn").fachdstSb("S165-fachdstSb")
                        .fachdstTelnr("S165-fachdstTelnr").fehler("S165-fehler")
                        .build()))
                .satzart200(List.of(Satzart200.builder()
                        .satzart("200").psobkey("S200-psobkey").einnahmeart("S200-einnahmeart").betrw("S200-betrw")
                        .faedn("S200-faedn").bldat("S200-bldat").persl("S200-persl").optxt("S200-optxt")
                        .sgtxt("S200-sgtxt").blwae("S200-blwae").xblnr("S200-xblnr").fvBelnr("S200-fvBelnr")
                        .kostl("S200-kostl").fehler("S200-fehler").mwskz("S200-mwskz").aufnr("S200-aufnr")
                        .build()))
                .satzart210(List.of(Satzart210.builder()
                        .satzart("210").psobkey("S210-psobkey").einnahmeart("S210-einnahmeart").betrw("S210-betrw")
                        .faedn("S210-faedn").bldat("S210-bldat").persl("S210-persl").xblnr("S210-xblnr")
                        .fvBelnr("S210-fvBelnr").blwae("S210-blwae").fehler("S210-fehler").kostl("S210-kostl")
                        .mwskz("S210-mwskz").aufnr("S210-aufnr")
                        .build()))
                .satzart250(List.of(Satzart250.builder()
                        .satzart("250").psobkey("S250-psobkey").einnahmeart("S250-einnahmeart").betrw("S250-betrw")
                        .faedn("S250-faedn").bldat("S250-bldat").persl("S250-persl").optxt("S250-optxt")
                        .sgtxt("S250-sgtxt").blwae("S250-blwae").valut("S250-valut").xblnr("S250-xblnr")
                        .fvBelnr("S250-fvBelnr").kostl("S250-kostl").fehler("S250-fehler").mwskz("S250-mwskz")
                        .aufnr("S250-aufnr")
                        .build()))
                .satzart260(List.of(Satzart260.builder()
                        .satzart("260").psobkey("S260-psobkey").einnahmeart("S260-einnahmeart").betrw("S260-betrw")
                        .faedn("S260-faedn").bldat("S260-bldat").persl("S260-persl").xblnr("S260-xblnr")
                        .fvBelnr("S260-fvBelnr").fehler("S260-fehler").kostl("S260-kostl").mwskz("S260-mwskz")
                        .aufnr("S260-aufnr")
                        .build()))
                .satzartFehler(List.of(SatzartFehler.builder()
                        .satzart("SF-satzart").fehlertext("SF-fehlertext")
                        .build()))
                .build();
    }

    /** The same batch as JSON. Property names are the generated model's, i.e. the schema's own. */
    private static String jsonPayload() {
        return """
                {
                  "filename": "%s",
                  "satzart010": { "satzart": "010", "abstimmsumme": "S010-abstimmsumme",
                    "vorzeichen": "S010-vorzeichen", "fehler": "S010-fehler" },
                  "satzart100": [ { "satzart": "100", "psobkey": "S100-psobkey", "partner": "S100-partner",
                    "addrnum": "S100-addrnum", "psobtxtb1": "S100-psobtxtb1", "psobtxtb2": "S100-psobtxtb2",
                    "psobtxtb3": "S100-psobtxtb3", "psobtxtb4": "S100-psobtxtb4", "psobtxtb5": "S100-psobtxtb5",
                    "psobtxtb6": "S100-psobtxtb6", "zweitschuldner": "S100-zweitschuldner",
                    "eigentuemerwechsel": "S100-eigentuemerwechsel", "betriebsende": "S100-betriebsende",
                    "kdKenn": "S100-kdKenn", "fachdstSb": "S100-fachdstSb", "fachdstTelnr": "S100-fachdstTelnr",
                    "fehler": "S100-fehler" } ],
                  "satzart105": [ { "satzart": "105", "psobkey": "S105-psobkey", "corrPartner": "S105-corrPartner",
                    "corrRole": "S105-corrRole", "fehler": "S105-fehler" } ],
                  "satzart155": [ { "satzart": "155", "psobkey": "S155-psobkey", "deleteFlag": "S155-deleteFlag",
                    "corrRole": "S155-corrRole", "fehler": "S155-fehler" } ],
                  "satzart165": [ { "satzart": "165", "psobkey": "S165-psobkey", "psobtxtb1": "S165-psobtxtb1",
                    "psobtxtb2": "S165-psobtxtb2", "psobtxtb3": "S165-psobtxtb3", "psobtxtb4": "S165-psobtxtb4",
                    "psobtxtb5": "S165-psobtxtb5", "psobtxtb6": "S165-psobtxtb6",
                    "zweitschuldner": "S165-zweitschuldner", "eigentuemerwechsel": "S165-eigentuemerwechsel",
                    "betriebsende": "S165-betriebsende", "kdKenn": "S165-kdKenn", "fachdstSb": "S165-fachdstSb",
                    "fachdstTelnr": "S165-fachdstTelnr", "fehler": "S165-fehler" } ],
                  "satzart200": [ { "satzart": "200", "psobkey": "S200-psobkey", "einnahmeart": "S200-einnahmeart",
                    "betrw": "S200-betrw", "faedn": "S200-faedn", "bldat": "S200-bldat", "persl": "S200-persl",
                    "optxt": "S200-optxt", "sgtxt": "S200-sgtxt", "blwae": "S200-blwae", "xblnr": "S200-xblnr",
                    "fvBelnr": "S200-fvBelnr", "kostl": "S200-kostl", "fehler": "S200-fehler",
                    "mwskz": "S200-mwskz", "aufnr": "S200-aufnr" } ],
                  "satzart210": [ { "satzart": "210", "psobkey": "S210-psobkey", "einnahmeart": "S210-einnahmeart",
                    "betrw": "S210-betrw", "faedn": "S210-faedn", "bldat": "S210-bldat", "persl": "S210-persl",
                    "xblnr": "S210-xblnr", "fvBelnr": "S210-fvBelnr", "blwae": "S210-blwae",
                    "fehler": "S210-fehler", "kostl": "S210-kostl", "mwskz": "S210-mwskz", "aufnr": "S210-aufnr" } ],
                  "satzart250": [ { "satzart": "250", "psobkey": "S250-psobkey", "einnahmeart": "S250-einnahmeart",
                    "betrw": "S250-betrw", "faedn": "S250-faedn", "bldat": "S250-bldat", "persl": "S250-persl",
                    "optxt": "S250-optxt", "sgtxt": "S250-sgtxt", "blwae": "S250-blwae", "valut": "S250-valut",
                    "xblnr": "S250-xblnr", "fvBelnr": "S250-fvBelnr", "kostl": "S250-kostl",
                    "fehler": "S250-fehler", "mwskz": "S250-mwskz", "aufnr": "S250-aufnr" } ],
                  "satzart260": [ { "satzart": "260", "psobkey": "S260-psobkey", "einnahmeart": "S260-einnahmeart",
                    "betrw": "S260-betrw", "faedn": "S260-faedn", "bldat": "S260-bldat", "persl": "S260-persl",
                    "xblnr": "S260-xblnr", "fvBelnr": "S260-fvBelnr", "fehler": "S260-fehler",
                    "kostl": "S260-kostl", "mwskz": "S260-mwskz", "aufnr": "S260-aufnr" } ],
                  "satzartFehler": [ { "satzart": "SF-satzart", "fehlertext": "SF-fehlertext" } ]
                }
                """.formatted(FILENAME);
    }

    /**
     * The same batch as the SOAP payload. Element order follows the schema's sequences, since JAXB
     * unmarshals a sequence in order, so this is part of the contract and not a formatting choice.
     */
    private static String xmlPayload() {
        return """
                <pscdBatch xmlns="https://refarch.muenchen.de/pscd/inbound/v1">
                  <filename>%s</filename>
                  <satzart010>
                    <satzart>010</satzart><abstimmsumme>S010-abstimmsumme</abstimmsumme>
                    <vorzeichen>S010-vorzeichen</vorzeichen><fehler>S010-fehler</fehler>
                  </satzart010>
                  <satzart100>
                    <satzart>100</satzart><psobkey>S100-psobkey</psobkey><partner>S100-partner</partner>
                    <addrnum>S100-addrnum</addrnum>
                    <psobtxtb1>S100-psobtxtb1</psobtxtb1><psobtxtb2>S100-psobtxtb2</psobtxtb2>
                    <psobtxtb3>S100-psobtxtb3</psobtxtb3><psobtxtb4>S100-psobtxtb4</psobtxtb4>
                    <psobtxtb5>S100-psobtxtb5</psobtxtb5><psobtxtb6>S100-psobtxtb6</psobtxtb6>
                    <zweitschuldner>S100-zweitschuldner</zweitschuldner>
                    <eigentuemerwechsel>S100-eigentuemerwechsel</eigentuemerwechsel>
                    <betriebsende>S100-betriebsende</betriebsende><kdKenn>S100-kdKenn</kdKenn>
                    <fachdstSb>S100-fachdstSb</fachdstSb><fachdstTelnr>S100-fachdstTelnr</fachdstTelnr>
                    <fehler>S100-fehler</fehler>
                  </satzart100>
                  <satzart105>
                    <satzart>105</satzart><psobkey>S105-psobkey</psobkey>
                    <corrPartner>S105-corrPartner</corrPartner><corrRole>S105-corrRole</corrRole>
                    <fehler>S105-fehler</fehler>
                  </satzart105>
                  <satzart155>
                    <satzart>155</satzart><psobkey>S155-psobkey</psobkey>
                    <deleteFlag>S155-deleteFlag</deleteFlag><corrRole>S155-corrRole</corrRole>
                    <fehler>S155-fehler</fehler>
                  </satzart155>
                  <satzart165>
                    <satzart>165</satzart><psobkey>S165-psobkey</psobkey>
                    <psobtxtb1>S165-psobtxtb1</psobtxtb1><psobtxtb2>S165-psobtxtb2</psobtxtb2>
                    <psobtxtb3>S165-psobtxtb3</psobtxtb3><psobtxtb4>S165-psobtxtb4</psobtxtb4>
                    <psobtxtb5>S165-psobtxtb5</psobtxtb5><psobtxtb6>S165-psobtxtb6</psobtxtb6>
                    <zweitschuldner>S165-zweitschuldner</zweitschuldner>
                    <eigentuemerwechsel>S165-eigentuemerwechsel</eigentuemerwechsel>
                    <betriebsende>S165-betriebsende</betriebsende><kdKenn>S165-kdKenn</kdKenn>
                    <fachdstSb>S165-fachdstSb</fachdstSb><fachdstTelnr>S165-fachdstTelnr</fachdstTelnr>
                    <fehler>S165-fehler</fehler>
                  </satzart165>
                  <satzart200>
                    <satzart>200</satzart><psobkey>S200-psobkey</psobkey>
                    <einnahmeart>S200-einnahmeart</einnahmeart><betrw>S200-betrw</betrw>
                    <faedn>S200-faedn</faedn><bldat>S200-bldat</bldat><persl>S200-persl</persl>
                    <optxt>S200-optxt</optxt><sgtxt>S200-sgtxt</sgtxt><blwae>S200-blwae</blwae>
                    <xblnr>S200-xblnr</xblnr><fvBelnr>S200-fvBelnr</fvBelnr><kostl>S200-kostl</kostl>
                    <fehler>S200-fehler</fehler><mwskz>S200-mwskz</mwskz><aufnr>S200-aufnr</aufnr>
                  </satzart200>
                  <satzart210>
                    <satzart>210</satzart><psobkey>S210-psobkey</psobkey>
                    <einnahmeart>S210-einnahmeart</einnahmeart><betrw>S210-betrw</betrw>
                    <faedn>S210-faedn</faedn><bldat>S210-bldat</bldat><persl>S210-persl</persl>
                    <xblnr>S210-xblnr</xblnr><fvBelnr>S210-fvBelnr</fvBelnr><blwae>S210-blwae</blwae>
                    <fehler>S210-fehler</fehler><kostl>S210-kostl</kostl><mwskz>S210-mwskz</mwskz>
                    <aufnr>S210-aufnr</aufnr>
                  </satzart210>
                  <satzart250>
                    <satzart>250</satzart><psobkey>S250-psobkey</psobkey>
                    <einnahmeart>S250-einnahmeart</einnahmeart><betrw>S250-betrw</betrw>
                    <faedn>S250-faedn</faedn><bldat>S250-bldat</bldat><persl>S250-persl</persl>
                    <optxt>S250-optxt</optxt><sgtxt>S250-sgtxt</sgtxt><blwae>S250-blwae</blwae>
                    <valut>S250-valut</valut><xblnr>S250-xblnr</xblnr><fvBelnr>S250-fvBelnr</fvBelnr>
                    <kostl>S250-kostl</kostl><fehler>S250-fehler</fehler><mwskz>S250-mwskz</mwskz>
                    <aufnr>S250-aufnr</aufnr>
                  </satzart250>
                  <satzart260>
                    <satzart>260</satzart><psobkey>S260-psobkey</psobkey>
                    <einnahmeart>S260-einnahmeart</einnahmeart><betrw>S260-betrw</betrw>
                    <faedn>S260-faedn</faedn><bldat>S260-bldat</bldat><persl>S260-persl</persl>
                    <xblnr>S260-xblnr</xblnr><fvBelnr>S260-fvBelnr</fvBelnr><fehler>S260-fehler</fehler>
                    <kostl>S260-kostl</kostl><mwskz>S260-mwskz</mwskz><aufnr>S260-aufnr</aufnr>
                  </satzart260>
                  <satzartFehler>
                    <satzart>SF-satzart</satzart><fehlertext>SF-fehlertext</fehlertext>
                  </satzartFehler>
                </pscdBatch>
                """.formatted(FILENAME);
    }
}
