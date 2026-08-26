package de.muenchen.oss.refarch.gateway.filter;

import de.muenchen.oss.refarch.gateway.OAuthSecurityMockConfiguration;
import de.muenchen.oss.refarch.gateway.SessionAccessConfiguration;
import de.muenchen.oss.refarch.gateway.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { TestConstants.SPRING_TEST_PROFILE, "redis-session" })
@TestPropertySource(
        // configure to a non existent redis host
        properties = {
                "spring.data.redis.port=1"
        }
)
@Import({ OAuthSecurityMockConfiguration.class, SessionAccessConfiguration.class })
@AutoConfigureWebTestClient
class RedisSessionUnavailableWebFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void redisSessionConnectionFailureReturnsServiceUnavailable() {
        webTestClient.get().uri("/actuator/info").exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

}
