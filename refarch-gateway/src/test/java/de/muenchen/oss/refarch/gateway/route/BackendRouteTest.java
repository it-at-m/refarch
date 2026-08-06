package de.muenchen.oss.refarch.gateway.route;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static de.muenchen.oss.refarch.gateway.TestConstants.SPRING_TEST_PROFILE;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import de.muenchen.oss.refarch.gateway.OAuthSecurityMockConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SPRING_TEST_PROFILE)
@EnableWireMock
@Import(OAuthSecurityMockConfiguration.class)
@AutoConfigureWebTestClient
class BackendRouteTest {
    public static final String SESSION_COOKIE_NAME = "SESSION";
    public static final String XSRF_COOKIE_NAME = "XSRF-TOKEN";
    public static final String XSRF_HEADER_NAME = "X-XSRF-TOKEN";
    public static final String XSRF_VALUE = "4d82f9f1-41f6-4a09-994a-df99d30d1be9";
    private static final String TEST_KEY = "testkey";
    public static final String TEST_VALUE = "testvalue";
    private static final String TEST_JSON = "{ \"" + TEST_KEY + "\" : \"" + TEST_VALUE + "\" }";
    public static final String TEST_KEY_EXPRESSION = "$." + TEST_KEY;

    public static final String URI_PUBLIC = "/public/api/backend/test";
    public static final String URI_PUBLIC_EXTRA_PATTERN = "/api/backend/public/test";
    public static final String URI_CLIENTS = "/clients/api/backend/test";
    public static final String URI_CLIENTS_EXTRA_PATTERN = "/api/backend/clients/test";
    public static final String URI_API = "/api/backend/test";
    public static final String BACKEND_URL = "/test";

    @Autowired
    private ApplicationContext context;
    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        // setup web test client
        webTestClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .build();
        // setup wiremock for routes
        stubFor(get(urlMatching(".*/test"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeaders(new HttpHeaders(
                                new HttpHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON),
                                new HttpHeader(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE,
                                        "Bearer realm=\"Access to the staging site\", charset=\"UTF-8\"") // removed by route filter
                        ))
                        .withBody(TEST_JSON)));
        stubFor(post(urlMatching(".*/test"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeaders(new HttpHeaders(
                                new HttpHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON),
                                new HttpHeader(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE,
                                        "Bearer realm=\"Access to the staging site\", charset=\"UTF-8\"") // removed by route filter
                        ))
                        .withBody(TEST_JSON)));
    }

    @Nested
    class ApiRouteTests {

        @Test
        @WithMockUser
        void apiGetSuccessWithoutXSRF() {
            webTestClient
                    .get().uri(URI_API)
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.OK)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                    .expectCookie().exists(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, getRequestedFor(urlEqualTo(BACKEND_URL))
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME)
                    .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern(ContentTypes.APPLICATION_JSON)));
        }

        @Test
        @WithMockUser
        void apiGetSuccessWithXSRF() {
            webTestClient
                    .get().uri(URI_API)
                    .cookie(XSRF_COOKIE_NAME, XSRF_VALUE)
                    .header(XSRF_HEADER_NAME, XSRF_VALUE)
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.OK)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, getRequestedFor(urlEqualTo(BACKEND_URL))
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME)
                    .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern(ContentTypes.APPLICATION_JSON)));
        }

        @Test
        void apiGetFound() {
            webTestClient
                    .get().uri(URI_API)
                    .exchange()
                    // because redirect to login
                    .expectStatus().isFound()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.LOCATION, ".*/login.*");

            verify(0, getRequestedFor(urlEqualTo(BACKEND_URL)));
        }

        @Test
        @WithMockUser
        void apiPostSuccess() {
            webTestClient
                    .post().uri(URI_API)
                    .cookie(XSRF_COOKIE_NAME, XSRF_VALUE)
                    .header(XSRF_HEADER_NAME, XSRF_VALUE)
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.OK)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, postRequestedFor(urlEqualTo(BACKEND_URL))
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME)
                    .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern(ContentTypes.APPLICATION_JSON)));
        }

        @Test
        void apiPostForbidden() {
            // No CSRF, no auth -> blocked by CSRF (403)
            webTestClient
                    .post().uri(URI_API)
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, postRequestedFor(urlEqualTo(BACKEND_URL)));
        }

        @Test
        void apiPostFound() {
            // With CSRF, but no auth -> redirected to login (302)
            webTestClient
                    .post().uri(URI_API)
                    .cookie(XSRF_COOKIE_NAME, XSRF_VALUE)
                    .header(XSRF_HEADER_NAME, XSRF_VALUE)
                    .exchange()
                    .expectStatus().isFound()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, postRequestedFor(urlEqualTo(BACKEND_URL)));
        }
    }

    @Nested
    class PublicRouteTests {
        @Test
        void publicGetSuccess() {
            webTestClient
                    .get().uri(URI_PUBLIC)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().exists(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, getRequestedFor(urlEqualTo(BACKEND_URL))
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void publicPostSuccess() {
            webTestClient
                    .post().uri(URI_PUBLIC)
                    .cookie(XSRF_COOKIE_NAME, XSRF_VALUE)
                    .header(XSRF_HEADER_NAME, XSRF_VALUE)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, postRequestedFor(anyUrl())
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void publicPostForbidden() {
            // No auth required, but missing CSRF token -> Forbidden
            webTestClient
                    .post().uri(URI_PUBLIC)
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, postRequestedFor(urlEqualTo(BACKEND_URL)));
        }

        @Test
        void publicGetSuccessExtraPattern() {
            webTestClient
                    .get().uri(URI_PUBLIC_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().exists(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, getRequestedFor(anyUrl())
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void publicPostFoundExtraPattern() {
            // With CSRF, non-available public pattern -> handles as authenticated endpoint -> redirected to login (302)
            webTestClient
                    .post().uri(URI_PUBLIC_EXTRA_PATTERN)
                    .cookie(XSRF_COOKIE_NAME, XSRF_VALUE)
                    .header(XSRF_HEADER_NAME, XSRF_VALUE)
                    .exchange()
                    .expectStatus().isFound()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.LOCATION, ".*/login.*");

            verify(0, postRequestedFor(anyUrl()));
        }
    }

    @Nested
    class ClientsRoutesTests {
        @Test
        void clientGetSuccess() {
            webTestClient
                    .mutateWith(mockJwt())
                    .get().uri(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, getRequestedFor(urlEqualTo(BACKEND_URL))
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void clientGetForbidden() {
            webTestClient
                    .get().uri(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, getRequestedFor(urlEqualTo(BACKEND_URL)));
        }

        @Test
        void clientPostSuccess() {
            // CSRF is disabled for client routes, so authenticated client POST requests succeed
            // without requiring a CSRF token.
            webTestClient
                    .mutateWith(mockJwt())
                    .post().uri(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, postRequestedFor(urlEqualTo(BACKEND_URL))
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void clientPostForbidden() {
            // CSRF is disabled for client routes, so missing CSRF no longer causes 403.
            // Without authentication, client POST requests must be rejected with 401.
            webTestClient
                    .post().uri(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, postRequestedFor(urlEqualTo(BACKEND_URL)));
        }

        @Test
        void clientGetSuccessExtraPattern() {
            webTestClient
                    .mutateWith(mockJwt())
                    .get().uri(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, getRequestedFor(anyUrl())
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void clientGetForbiddenExtraPattern() {
            webTestClient
                    .get().uri(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, getRequestedFor(anyUrl()));
        }

        @Test
        void clientPostSuccessExtraPattern() {
            webTestClient
                    .mutateWith(mockJwt())
                    .post().uri(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verify(1, postRequestedFor(anyUrl())
                    .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                    .withoutHeader(XSRF_HEADER_NAME));
        }

        @Test
        void clientPostForbiddenExtraPattern() {
            webTestClient
                    .post().uri(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verify(0, postRequestedFor(anyUrl()));
        }
    }
}
