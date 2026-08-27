package de.muenchen.oss.refarch.gateway.filter;

import static de.muenchen.oss.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.muenchen.oss.refarch.gateway.OAuthSecurityMockConfiguration;
import de.muenchen.oss.refarch.gateway.configuration.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@Import(OAuthSecurityMockConfiguration.class)
@AutoConfigureWebTestClient
class GlobalRequestParameterPollutionFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SecurityProperties securityProperties;

    @AfterEach
    void resetProperties() {
        securityProperties.setParameterPollutionWhitelisted(Set.of());
    }

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
        securityProperties.setParameterPollutionWhitelisted(Set.of("otherParameter"));

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
        securityProperties.setParameterPollutionWhitelisted(Set.of("parameter1"));
        final String url = "/api/backend/testendpoint?parameter1=testdata_1&parameter2=testdata&parameter1=testdata_2";

        webTestClient.get().uri(url).exchange()
                .expectStatus()
                .is5xxServerError();
    }

}
