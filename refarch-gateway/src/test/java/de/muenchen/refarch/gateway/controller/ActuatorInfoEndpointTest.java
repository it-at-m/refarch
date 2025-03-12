package de.muenchen.refarch.gateway.controller;

import static de.muenchen.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;

import de.muenchen.refarch.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@ActiveProfiles(SPRING_TEST_PROFILE)
@AutoConfigureWebTestClient
@Import(OAuthSecurityMockConfiguration.class)
class ActuatorInfoEndpointTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void actuatorInfo() {
        webTestClient.get().uri("/actuator/info").exchange()
                .expectStatus().isOk();
    }

}
