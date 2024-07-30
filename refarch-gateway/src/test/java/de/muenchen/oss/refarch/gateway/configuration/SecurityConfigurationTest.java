package de.muenchen.oss.refarch.gateway.configuration;

import static de.muenchen.oss.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureObservability
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
public class SecurityConfigurationTest {
    @Autowired
    WebTestClient api;

    @Test
    void accessSecuredResourceRootThenUnauthorized() {
        // 302 is returned instead of 401 because auf cookie session
        api.get().uri("/").exchange().expectStatus().isFound();
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
