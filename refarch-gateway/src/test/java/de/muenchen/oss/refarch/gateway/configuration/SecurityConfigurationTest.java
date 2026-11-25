package de.muenchen.oss.refarch.gateway.configuration;

import de.muenchen.oss.refarch.gateway.OAuthSecurityMockConfiguration;
import de.muenchen.oss.refarch.gateway.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE })
@Import(OAuthSecurityMockConfiguration.class)
@AutoConfigureWebTestClient
class SecurityConfigurationTest {

    @Autowired
    private WebTestClient api;

    @ParameterizedTest
    @CsvSource(
        {
                "/api, application/json",
                "/api, text/html",
                "/api/test, application/json",
                "/api/test, text/html",
                "/api/backend/nested/test, application/json",
                "/api/backend/nested/test, text/html"
        }
    )
    void accessSecuredApiThenUnauthorizedWithoutLoginRedirect(final String path, final String accept) {
        api.get().uri(path).accept(MediaType.parseMediaType(accept))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().doesNotExist(HttpHeaders.LOCATION);
    }

    @ParameterizedTest
    @CsvSource(
        {
                "/, application/json",
                "/, text/html",
                "/frontend/test, application/json",
                "/frontend/test, text/html",
                "/apiary/test, application/json",
                "/apiary/test, text/html"
        }
    )
    void accessSecuredFrontendThenLoginRedirect(final String path, final String accept) {
        // Frontend requests retain the session-based login redirect regardless of Accept.
        api.get().uri(path).accept(MediaType.parseMediaType(accept))
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/login");
    }

    @Test
    void accessSecuredResourceClientsThenUnauthorized() {
        api.get().uri("/clients/test").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthThenOk() {
        api.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthLivenessThenOk() {
        api.get().uri("/actuator/health/liveness").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorHealthReadinessThenOk() {
        api.get().uri("/actuator/health/readiness").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorInfoThenOk() {
        api.get().uri("/actuator/info").exchange().expectStatus().isOk();
    }

    @Test
    void accessUnsecuredResourceActuatorMetricsThenOk() {
        api.get().uri("/actuator/metrics").exchange().expectStatus().isOk();
    }
}
