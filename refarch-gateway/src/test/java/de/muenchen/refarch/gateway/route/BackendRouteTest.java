package de.muenchen.refarch.gateway.route;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static de.muenchen.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import de.muenchen.refarch.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@ActiveProfiles(SPRING_TEST_PROFILE)
@AutoConfigureWireMock
@AutoConfigureWebTestClient
@Import(OAuthSecurityMockConfiguration.class)
class BackendRouteTest {

    public static final String XSRF_HEADER_NAME = "X-XSRF-TOKEN";
    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        stubFor(get(urlEqualTo("/remote/endpoint"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeaders(new HttpHeaders(
                                new HttpHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json"),
                                new HttpHeader(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE,
                                        "Bearer realm=\"Access to the staging site\", charset=\"UTF-8\"") // removed by route filter
                        ))
                        .withBody("{ \"testkey\" : \"testvalue\" }")));
    }

    @Test
    @WithMockUser
    void backendRouteResponse() {
        webTestClient.get().uri("/api/refarch-gateway-backend-service/remote/endpoint")
                .header(org.springframework.http.HttpHeaders.COOKIE,
                        "SESSION=5cfb01a3-b691-4ca9-8735-a05690e6c2ec; XSRF-TOKEN=4d82f9f1-41f6-4a09-994a-df99d30d1be9") // removed by default-filter
                .header(XSRF_HEADER_NAME, "5cfb01a3-b691-4ca9-8735-a05690e6c2ec") // angular specific -> removed by default-filter
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/hal+json")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/json")
                .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                .expectBody().jsonPath("$.testkey").isEqualTo("testvalue");

        verify(getRequestedFor(urlEqualTo("/remote/endpoint"))
                .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                .withoutHeader(XSRF_HEADER_NAME)
                .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern("application/hal+json")));
    }

}
