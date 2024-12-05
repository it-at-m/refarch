package de.muenchen.refarch.gateway.filter;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static de.muenchen.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import de.muenchen.refarch.gateway.ApiGatewayApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = { ApiGatewayApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles(SPRING_TEST_PROFILE)
@AutoConfigureWireMock
class GlobalAuthenticationErrorFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        stubFor(get(urlEqualTo("/remote"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.UNAUTHORIZED.value())
                        .withHeaders(new HttpHeaders(
                                new HttpHeader("Content-Type", "application/json"),
                                new HttpHeader("WWW-Authenticate", "Bearer realm=\"Access to the staging site\", charset=\"UTF-8\"")))
                        .withBody("{ \"testkey\" : \"testvalue\" }")));
    }

    @Test
    @WithMockUser
    void backendAuthenticationError() {
        webTestClient.get().uri("/api/refarch-gateway-backend-service/remote").exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
                .expectHeader().valueMatches("Content-Type", "application/json")
                .expectHeader().doesNotExist("WWW-Authenticate")
                .expectBody()
                .jsonPath("$.status").isEqualTo("401")
                .jsonPath("$.error").isEqualTo("Authentication Error");
    }

}
