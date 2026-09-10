package de.muenchen.oss.refarch.integration.pscd.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSOAPSatzarten;
import de.lhm.pi.pscd.rahmenschnittstelle.DTSatzart010;
import org.junit.jupiter.api.Test;

@WireMockTest
class PscdSoapClientTest {

    private static final String PATH = "/pscd";

    /** The batch every message in here carries, so an assertion can find it in the body. */
    private static final String FILENAME = "records.dat";

    @Test
    void sendsOneWayMessage(final WireMockRuntimeInfo wireMock) {
        // A one-way SOAP operation expects an (empty) HTTP 200/202 acknowledgement, no SOAP body.
        stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(202)));

        final PscdSoapClient client = new PscdSoapClient(endpoint(wireMock));

        assertThatCode(() -> client.send(message(FILENAME))).doesNotThrowAnyException();
        verify(postRequestedFor(urlEqualTo(PATH))
                .withRequestBody(containing("MT_SOAPSatzarten"))
                .withRequestBody(containing("records.dat")));
    }

    /**
     * Credentials go out preemptively: the stub answers the very first request, so a client that waited
     * for a {@code 401} challenge would send nothing here and fail the verification.
     */
    @Test
    void sendsConfiguredCredentialsAsHttpBasic(final WireMockRuntimeInfo wireMock) {
        stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(202)));

        final PscdSoapClient client = new PscdSoapClient(endpoint(wireMock),
                "pscd-service", "s3cret");

        client.send(message(FILENAME));

        verify(postRequestedFor(urlEqualTo(PATH)).withBasicAuth(new BasicCredentials("pscd-service", "s3cret")));
    }

    @Test
    void sendsNoAuthorizationHeaderWithoutAUsername(final WireMockRuntimeInfo wireMock) {
        stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(202)));

        new PscdSoapClient(endpoint(wireMock)).send(message(FILENAME));

        verify(postRequestedFor(urlEqualTo(PATH)).withoutHeader("Authorization"));
    }

    /**
     * A blank username is a half-configured deployment, not a credential: sending
     * {@code Basic <base64 of ":password">} would let the endpoint reject a request that was never
     * meant to carry one.
     */
    @Test
    void sendsNoAuthorizationHeaderForABlankUsername(final WireMockRuntimeInfo wireMock) {
        stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(202)));

        new PscdSoapClient(endpoint(wireMock), "  ", "s3cret")
                .send(message(FILENAME));

        verify(postRequestedFor(urlEqualTo(PATH)).withoutHeader("Authorization"));
    }

    @Test
    void throwsWhenEndpointReturnsServerError(final WireMockRuntimeInfo wireMock) {
        stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(500).withBody("boom")));

        final PscdSoapClient client = new PscdSoapClient(endpoint(wireMock));

        assertThatThrownBy(() -> client.send(message(FILENAME))).isInstanceOf(RuntimeException.class);
    }

    /** The stub endpoint's address, on whichever port WireMock took. */
    private static String endpoint(final WireMockRuntimeInfo wireMock) {
        return "http://localhost:" + wireMock.getHttpPort() + PATH;
    }

    private static DTSOAPSatzarten message(final String filename) {
        final DTSOAPSatzarten satzarten = new DTSOAPSatzarten();
        satzarten.setFilename(filename);
        final DTSatzart010 control = new DTSatzart010();
        control.setSATZART("010");
        control.setABSTIMMSUMME("0");
        control.setVORZEICHEN("+");
        satzarten.setSatzart010(control);
        return satzarten;
    }
}
