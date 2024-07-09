package de.muenchen.oss.refarch.gateway.controller;

import de.muenchen.oss.refarch.gateway.ApiGatewayApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static de.muenchen.oss.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = {ApiGatewayApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles(SPRING_TEST_PROFILE)
public class ActuatorInfoEndpointTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void actuatorInfoProvidesAppswitcherUrl() {
        webTestClient.get().uri("/actuator/info").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.appswitcher.url").isEqualTo("https://test-url-appswitcher.muenchen.de");
    }

}
