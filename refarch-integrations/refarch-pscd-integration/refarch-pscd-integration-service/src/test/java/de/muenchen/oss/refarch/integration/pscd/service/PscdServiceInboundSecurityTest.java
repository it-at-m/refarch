package de.muenchen.oss.refarch.integration.pscd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import de.muenchen.oss.refarch.integration.pscd.application.port.out.PscdOutPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * HTTP Basic in front of both HTTP channels. Driven over a real port rather than with MockMvc: the
 * filter chain sits in the servlet container, and the SOAP edge runs in its own
 * {@code MessageDispatcherServlet} that MockMvc never reaches.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "refarch.pscd.inbound.soap.enabled=true",
                "refarch.pscd.inbound.rest.enabled=true",
                "refarch.pscd.inbound.file.enabled=false",
                "refarch.pscd.inbound.security.username=pscd-sender",
                "refarch.pscd.inbound.security.password=s3cret"
        }
)
@ExtendWith(OutputCaptureExtension.class)
class PscdServiceInboundSecurityTest {

    private static final String REST_ADDRESS = "/api/pscd/batches";
    private static final String JSON = "application/json";

    /** The account this context configures; the rejection cases present something else, or nothing. */
    private static final String USERNAME = "pscd-sender";
    private static final String PASSWORD = "s3cret";
    private static final String SOAP_ADDRESS = "/ws/pscd";

    private static final String BATCH_JSON = """
            {
              "filename": "secured.json",
              "satzart010": { "satzart": "010", "abstimmsumme": "12345", "vorzeichen": "+" }
            }
            """;

    private static final String BATCH_ENVELOPE = """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
              <soapenv:Body>
                <pscdBatch xmlns="https://refarch.muenchen.de/pscd/inbound/v1">
                  <filename>secured.xml</filename>
                  <satzart010>
                    <satzart>010</satzart>
                    <abstimmsumme>12345</abstimmsumme>
                    <vorzeichen>+</vorzeichen>
                  </satzart010>
                </pscdBatch>
              </soapenv:Body>
            </soapenv:Envelope>
            """;

    // Terminate the pipeline here: what is under test is who gets through the filter chain, not delivery.
    @MockitoBean
    private PscdOutPort pscdOutPort;

    @Value("${local.server.port}")
    private int port;

    @Test
    void rejectsARestSubmissionWithoutCredentials() throws Exception {
        final HttpResponse<String> response = post(REST_ADDRESS, JSON, BATCH_JSON, null);

        assertThat(response.statusCode()).isEqualTo(401);
        // Named realm, so a sender sees what it is being asked for.
        assertThat(response.headers().firstValue("WWW-Authenticate")).hasValueSatisfying(
                header -> assertThat(header).contains("PSCD inbound"));
        verify(this.pscdOutPort, never()).send(any());
    }

    @Test
    void acceptsARestSubmissionWithTheConfiguredCredentials() throws Exception {
        final HttpResponse<String> response = post(REST_ADDRESS, JSON, BATCH_JSON,
                basic(USERNAME, PASSWORD));

        assertThat(response.statusCode()).isEqualTo(202);
        verify(this.pscdOutPort).send(any());
    }

    @Test
    void rejectsARestSubmissionWithTheWrongPassword() throws Exception {
        final HttpResponse<String> response = post(REST_ADDRESS, JSON, BATCH_JSON,
                basic(USERNAME, "wrong"));

        assertThat(response.statusCode()).isEqualTo(401);
        verify(this.pscdOutPort, never()).send(any());
    }

    @Test
    void rejectsASoapSubmissionWithoutCredentials() throws Exception {
        final HttpResponse<String> response = post(SOAP_ADDRESS, "text/xml", BATCH_ENVELOPE, null);

        assertThat(response.statusCode()).isEqualTo(401);
        verify(this.pscdOutPort, never()).send(any());
    }

    @Test
    void acceptsASoapSubmissionWithTheConfiguredCredentials() throws Exception {
        final HttpResponse<String> response = post(SOAP_ADDRESS, "text/xml", BATCH_ENVELOPE,
                basic(USERNAME, PASSWORD));

        // One-way operation: an empty acknowledgement, and the batch reached the pipeline.
        assertThat(response.statusCode()).isIn(200, 202);
        verify(this.pscdOutPort).send(any());
    }

    /**
     * The contract stays public: a sending system is built against these before it has an account, and
     * tooling fetches them unauthenticated.
     */
    @Test
    void servesTheContractArtefactsWithoutCredentials() throws Exception {
        final HttpResponse<String> wsdl = get(SOAP_ADDRESS + "/pscdInbound.wsdl");
        assertThat(wsdl.statusCode()).isEqualTo(200);
        assertThat(wsdl.body()).contains("PscdInboundPort");

        final HttpResponse<String> schema = get(SOAP_ADDRESS + "/pscdCanonical.xsd");
        assertThat(schema.statusCode()).isEqualTo(200);
        assertThat(schema.body()).contains("name=\"pscdBatch\"");

        final HttpResponse<String> openApi = get("/v3/api-docs");
        assertThat(openApi.statusCode()).isEqualTo(200);
        assertThat(openApi.body()).contains(REST_ADDRESS);
    }

    /**
     * Only reading is open. The SOAP address is also the address the WSDL hangs off, so the method is
     * what separates fetching the contract from submitting against it.
     */
    @Test
    void stillRejectsAnUnauthenticatedSubmissionAtTheOpenSoapAddress() throws Exception {
        assertThat(get(SOAP_ADDRESS + "/pscdInbound.wsdl").statusCode()).isEqualTo(200);

        assertThat(post(SOAP_ADDRESS, "text/xml", BATCH_ENVELOPE, null).statusCode()).isEqualTo(401);
        verify(this.pscdOutPort, never()).send(any());
    }

    /**
     * The audit trail, through the real filter chain: the wording is
     * {@code PscdInboundAuthenticationLog}'s own test, what this pins is that Spring Security actually
     * reaches it, on the success and failure events, and on the entry point for a request that
     * presented nothing.
     */
    @Test
    void logsEveryAuthenticationOutcome(final CapturedOutput output) throws Exception {
        post(REST_ADDRESS, JSON, BATCH_JSON, basic(USERNAME, PASSWORD));
        post(REST_ADDRESS, JSON, BATCH_JSON, basic(USERNAME, "wrong"));
        post(REST_ADDRESS, JSON, BATCH_JSON, null);

        assertThat(output).contains("PSCD inbound authenticated user 'pscd-sender'");
        assertThat(output).contains("PSCD inbound rejected user 'pscd-sender'");
        assertThat(output).contains("BadCredentialsException");
        assertThat(output).contains("rejected an unauthenticated POST " + REST_ADDRESS);
    }

    private HttpResponse<String> get(final String path) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    private HttpResponse<String> post(final String path, final String contentType, final String body,
            final String authorization) throws Exception {
        final HttpRequest.Builder request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    private URI uri(final String path) {
        return URI.create("http://localhost:" + this.port + path);
    }

    private static String basic(final String username, final String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ':' + password).getBytes(StandardCharsets.UTF_8));
    }
}
