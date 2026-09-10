package de.muenchen.oss.refarch.integration.pscd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.serverOrReceiverFault;

import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import de.muenchen.oss.refarch.integration.pscd.domain.model.PscdSatzarten;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

// Isolate the SOAP channel: only soap on. RANDOM_PORT gives a real servlet container, which the
// MessageDispatcherServlet mapping and the WSDL/XSD publication checks below need.
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "refarch.pscd.inbound.soap.enabled=true",
                "refarch.pscd.inbound.rest.enabled=false",
                "refarch.pscd.inbound.file.enabled=false",
                // Mandatory: the HTTP channels always require HTTP Basic and have no default account.
                "refarch.pscd.inbound.security.username=pscd-sender",
                "refarch.pscd.inbound.security.password=s3cret"
        }
)
class PscdServiceSoapModeTest {

    private static final String SOAP_ADDRESS = "/ws/pscd";

    // elementFormDefault is "qualified", so the default namespace on the root covers every child.
    private static final String PAYLOAD = """
            <pscdBatch xmlns="https://refarch.muenchen.de/pscd/inbound/v1">
              <filename>soap-batch.xml</filename>
              <satzart010>
                <satzart>010</satzart>
                <abstimmsumme>12345</abstimmsumme>
                <vorzeichen>+</vorzeichen>
              </satzart010>
              <satzart100>
                <satzart>100</satzart>
                <psobkey>KEY1</psobkey>
                <partner>PARTNER1</partner>
              </satzart100>
              <satzart200>
                <satzart>200</satzart>
                <psobkey>KEY2</psobkey>
                <einnahmeart>EA</einnahmeart>
                <betrw>100,00</betrw>
                <faedn>20260131</faedn>
                <bldat>20260101</bldat>
                <xblnr>INV-1</xblnr>
                <fvBelnr>FV-1</fvBelnr>
              </satzart200>
              <satzart200>
                <satzart>200</satzart>
                <psobkey>KEY3</psobkey>
                <einnahmeart>EA</einnahmeart>
                <betrw>50,00</betrw>
                <faedn>20260228</faedn>
                <bldat>20260101</bldat>
                <xblnr>INV-2</xblnr>
                <fvBelnr>FV-2</fvBelnr>
              </satzart200>
            </pscdBatch>
            """;

    @Autowired
    private ApplicationContext applicationContext;

    // Terminate the pipeline at the out-port: these tests exercise the inbound edge, not delivery to
    // the real PSCD endpoint (which refarch.pscd.client.url points at and which is not running).
    @MockitoBean
    private PscdOutPort pscdOutPort;

    @Value("${local.server.port}")
    private int port;

    @Test
    void publishesTheSoapEndpointAndNotTheFilePoller() {
        assertThat(this.applicationContext.containsBean("pscdSoapInboundEndpoint")).isTrue();
        assertThat(this.applicationContext.containsBean("pscdFilePoller")).isFalse();
    }

    /**
     * The canonical contract must be reachable at the documented address: the WSDL, plus the schema it
     * includes at the sibling URL its {@code xsd:include} names. Spring WS serves the WSDL verbatim, so
     * a wrong {@code schemaLocation} would leave consumers unable to resolve the types.
     */
    @Test
    void servesTheCanonicalContractWsdlAndSchemaAtTheSoapAddress() throws Exception {
        final HttpResponse<String> wsdl = get(SOAP_ADDRESS + "/pscdInbound.wsdl");

        assertThat(wsdl.statusCode()).isEqualTo(200);
        assertThat(wsdl.body())
                .contains("PscdInboundPort")
                .contains("https://refarch.muenchen.de/pscd/inbound/v1")
                .contains("schemaLocation=\"pscdCanonical.xsd\"");

        final HttpResponse<String> schema = get(SOAP_ADDRESS + "/pscdCanonical.xsd");

        assertThat(schema.statusCode()).isEqualTo(200);
        assertThat(schema.body()).contains("name=\"pscdBatch\"");
    }

    /**
     * Spring WS dispatches on the payload root rather than the path, so the servlet mapping
     * {@code /ws/pscd/*} has to match the bare {@code /ws/pscd} that the documented address, and every
     * existing caller, uses. Asserted over real HTTP because the
     * mapping is exactly what is under test here; the payload itself is covered by
     * {@link #acceptsACanonicalEnvelopeAndDeliversItToTheOutPort()}.
     */
    @Test
    void acceptsAPostAtTheDocumentedSoapAddress() throws Exception {
        final HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + SOAP_ADDRESS))
                            .header("Content-Type", "text/xml")
                            // The channel always requires HTTP Basic; these are application.yml's legacy
                            // defaults, which this context does not override.
                            .header("Authorization", "Basic "
                                    + Base64.getEncoder().encodeToString("pscd-sender:s3cret".getBytes(StandardCharsets.UTF_8)))
                            .POST(HttpRequest.BodyPublishers.ofString(envelope(), StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
        }

        // One-way operation: accepted with no response payload and, crucially, not a 404 from a
        // servlet mapping that failed to match the address without a trailing path segment.
        assertThat(response.statusCode()).isIn(200, 202);
    }

    /**
     * The payload path: {@code @PayloadRoot} resolution, JAXB binding of the canonical model and the
     * mapping onto the domain. The SOAP counterpart of
     * {@code PscdServiceRestModeTest.acceptsACanonicalJsonBatchAndDeliversItToTheOutPort}, asserting
     * the same fields. If the schema's global element and the endpoint's {@code localPart} ever drift
     * apart, this is what fails.
     */
    @Test
    void acceptsACanonicalEnvelopeAndDeliversItToTheOutPort() {
        final MockWebServiceClient wsClient = MockWebServiceClient.createClient(this.applicationContext);

        wsClient.sendRequest(withPayload(new StringSource(PAYLOAD)));

        final ArgumentCaptor<PscdSatzarten> captor = ArgumentCaptor.forClass(PscdSatzarten.class);
        verify(this.pscdOutPort, times(1)).send(captor.capture());
        final PscdSatzarten batch = captor.getValue();
        assertThat(batch.getFilename()).isEqualTo("soap-batch.xml");
        assertThat(batch.getSatzart010().getAbstimmsumme()).isEqualTo("12345");
        assertThat(batch.getSatzart100()).singleElement()
                .satisfies(record -> assertThat(record.getPartner()).isEqualTo("PARTNER1"));
        assertThat(batch.getSatzart200()).hasSize(2);
        assertThat(batch.getSatzart200().get(0).getFvBelnr()).isEqualTo("FV-1");
    }

    /**
     * The filename identifies the batch end to end, so a payload without one must not be delivered.
     * The schema declares the element mandatory, but no validating interceptor enforces it, so the
     * canonical mapper's guard is what turns the missing element into a fault. The SOAP counterpart
     * of the REST edge's 400.
     */
    @Test
    void rejectsABatchMissingTheMandatoryFilenameWithAFault() {
        final MockWebServiceClient wsClient = MockWebServiceClient.createClient(this.applicationContext);
        final String payload = """
                <pscdBatch xmlns="https://refarch.muenchen.de/pscd/inbound/v1">
                  <satzart010>
                    <satzart>010</satzart>
                    <abstimmsumme>1</abstimmsumme>
                    <vorzeichen>+</vorzeichen>
                  </satzart010>
                </pscdBatch>
                """;

        wsClient.sendRequest(withPayload(new StringSource(payload)))
                .andExpect(serverOrReceiverFault());

        verify(this.pscdOutPort, never()).send(any());
    }

    private HttpResponse<String> get(final String path) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path)).build(),
                    HttpResponse.BodyHandlers.ofString());
        }
    }

    private static String envelope() {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                + PAYLOAD
                + "</soap:Body></soap:Envelope>";
    }
}
