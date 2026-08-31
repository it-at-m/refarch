package de.muenchen.oss.refarch.gateway.filter;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static de.muenchen.oss.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import de.muenchen.oss.refarch.gateway.OAuthSecurityMockConfiguration;
import de.muenchen.oss.refarch.gateway.configuration.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@EnableWireMock
@Import(OAuthSecurityMockConfiguration.class)
@AutoConfigureWebTestClient
class GlobalRequestParameterPollutionFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SecurityProperties securityProperties;

    @Test
    @WithMockUser
    void parameterPollutionAttack() {
        final StringBuilder jsonResponseBody = new StringBuilder();
        final String url = "/api/backend/testendpoint?parameter1=testdata_1&parameter2=testdata&parameter1=testdata_2";
        webTestClient.get().uri(url).exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .consumeWith(responseBody -> jsonResponseBody.append(
                        new String(Objects.requireNonNull(responseBody.getResponseBody()), StandardCharsets.UTF_8)));
        assertTrue(jsonResponseBody.toString().contains("\"message\":\"parameter pollution\""));
    }

    @Test
    @WithMockUser
    void parameterPollutionAttack_WithUnmatchedWhitelist_Blocks() {
        when(securityProperties.getParameterPollutionWhitelisted()).thenReturn(Set.of("otherParameter"));

        final StringBuilder jsonResponseBody = new StringBuilder();
        final String url = "/api/backend/testendpoint?parameter1=testdata_1&parameter2=testdata&parameter1=testdata_2";

        webTestClient.get().uri(url).exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody()
                .consumeWith(responseBody -> jsonResponseBody.append(
                        new String(Objects.requireNonNull(responseBody.getResponseBody()), StandardCharsets.UTF_8)));

        assertTrue(jsonResponseBody.toString().contains("\"message\":\"parameter pollution\""));
    }

    @Test
    @WithMockUser
    void parameterPollutionAttack_WithMatchedWhitelist_Allows() {
        when(securityProperties.getParameterPollutionWhitelisted()).thenReturn(Set.of("parameter1"));

        stubFor(get(urlPathEqualTo("/testendpoint"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())));

        final String url = "/api/backend/testendpoint?parameter1=testdata_1&parameter2=testdata&parameter1=testdata_2";

        webTestClient.get().uri(url).exchange()
                .expectStatus()
                .isOk();
    }

}
