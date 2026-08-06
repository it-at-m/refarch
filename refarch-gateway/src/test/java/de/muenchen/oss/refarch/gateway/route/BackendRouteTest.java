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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
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
                .willReturn(okJsonBackendResponse()));
        stubFor(post(urlMatching(".*/test"))
                .willReturn(okJsonBackendResponse()));
    }

    @Nested
    class ApiRouteTests {

        @Test
        @WithMockUser
        void apiGetSuccessWithoutXSRF() {
            withJsonContentType(createGet(URI_API))
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.OK)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                    .expectCookie().exists(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendJsonGetWithoutCookies(1, urlEqualTo(BACKEND_URL));
        }

        @Test
        @WithMockUser
        void apiGetSuccessWithXSRF() {
            withJsonContentType(withXsrf(createGet(URI_API)))
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.OK)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendJsonGetWithoutCookies(1, urlEqualTo(BACKEND_URL));
        }

        @Test
        void apiGetFound() {
            createGet(URI_API)
                    .exchange()
                    // because redirect to login
                    .expectStatus().isFound()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.LOCATION, ".*/login.*");

            verifyBackendGetNotCalled(urlEqualTo(BACKEND_URL));
        }

        @Test
        @WithMockUser
        void apiPostSuccess() {
            withJsonContentType(withXsrf(createPost(URI_API)))
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.OK)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON)
                    .expectHeader().doesNotExist(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendJsonPostWithoutCookies(1, urlEqualTo(BACKEND_URL));
        }

        @Test
        void apiPostForbidden() {
            // No CSRF, no auth -> blocked by CSRF (403)
            createPost(URI_API)
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendPostNotCalled(urlEqualTo(BACKEND_URL));
        }

        @Test
        void apiPostFound() {
            // With CSRF, but no auth -> redirected to login (302)
            withXsrf(createPost(URI_API))
                    .exchange()
                    .expectStatus().isFound()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendPostNotCalled(urlEqualTo(BACKEND_URL));
        }
    }

    @Nested
    class PublicRouteTests {
        @Test
        void publicGetSuccess() {
            createGet(URI_PUBLIC)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().exists(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendGetWithoutCookies(1, urlEqualTo(BACKEND_URL));
        }

        @Test
        void publicPostSuccess() {
            withXsrf(createPost(URI_PUBLIC))
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendPostWithoutCookies(1, anyUrl());
        }

        @Test
        void publicPostForbidden() {
            // No auth required, but missing CSRF token -> Forbidden
            createPost(URI_PUBLIC)
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendPostNotCalled(urlEqualTo(BACKEND_URL));
        }

        @Test
        void publicGetSuccessExtraPattern() {
            createGet(URI_PUBLIC_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().exists(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendGetWithoutCookies(1, anyUrl());
        }

        @Test
        void publicPostFoundExtraPattern() {
            // With CSRF, non-available public pattern -> handles as authenticated endpoint -> redirected to login (302)
            withXsrf(createPost(URI_PUBLIC_EXTRA_PATTERN))
                    .exchange()
                    .expectStatus().isFound()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectHeader().valueMatches(org.springframework.http.HttpHeaders.LOCATION, ".*/login.*");

            verifyBackendPostNotCalled(anyUrl());
        }
    }

    @Nested
    class ClientsRoutesTests {
        @Test
        void clientGetSuccess() {
            createClientGet(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendGetWithoutCookies(1, urlEqualTo(BACKEND_URL));
        }

        @Test
        void clientGetForbidden() {
            createGet(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendGetNotCalled(urlEqualTo(BACKEND_URL));
        }

        @Test
        void clientPostSuccess() {
            // CSRF is disabled for client routes, so authenticated client POST requests succeed
            // without requiring a CSRF token.
            createClientPost(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendPostWithoutCookies(1, urlEqualTo(BACKEND_URL));
        }

        @Test
        void clientPostForbidden() {
            // CSRF is disabled for client routes, so missing CSRF no longer causes 403.
            // Without authentication, client POST requests must be rejected with 401.
            createPost(URI_CLIENTS)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendPostNotCalled(urlEqualTo(BACKEND_URL));
        }

        @Test
        void clientGetSuccessExtraPattern() {
            createClientGet(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendGetWithoutCookies(1, anyUrl());
        }

        @Test
        void clientGetForbiddenExtraPattern() {
            createGet(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendGetNotCalled(anyUrl());
        }

        @Test
        void clientPostSuccessExtraPattern() {
            createClientPost(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isOk()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME)
                    .expectBody().jsonPath(TEST_KEY_EXPRESSION).isEqualTo(TEST_VALUE);

            verifyBackendPostWithoutCookies(1, anyUrl());
        }

        @Test
        void clientPostForbiddenExtraPattern() {
            createPost(URI_CLIENTS_EXTRA_PATTERN)
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectCookie().doesNotExist(SESSION_COOKIE_NAME)
                    .expectCookie().doesNotExist(XSRF_COOKIE_NAME);

            verifyBackendPostNotCalled(anyUrl());
        }
    }

    private static ResponseDefinitionBuilder okJsonBackendResponse() {
        // common backend response used for both GET and POST
        return aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeaders(new HttpHeaders(
                        new HttpHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON),
                        new HttpHeader(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE,
                                "Bearer realm=\"Access to the staging site\", charset=\"UTF-8\"") // removed by route filter
                ))
                .withBody(TEST_JSON);
    }

    private static void verifyBackendJsonGetWithoutCookies(final int expectedCount, final UrlPattern urlPattern) {
        verify(expectedCount, getRequestedFor(urlPattern)
                .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                .withoutHeader(XSRF_HEADER_NAME)
                .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern(ContentTypes.APPLICATION_JSON)));
    }

    private static void verifyBackendJsonPostWithoutCookies(final int expectedCount, final UrlPattern urlPattern) {
        verify(expectedCount, postRequestedFor(urlPattern)
                .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                .withoutHeader(XSRF_HEADER_NAME)
                .withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, new EqualToPattern(ContentTypes.APPLICATION_JSON)));
    }

    private static void verifyBackendGetWithoutCookies(final int expectedCount, final UrlPattern urlPattern) {
        verify(expectedCount, getRequestedFor(urlPattern)
                .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                .withoutHeader(XSRF_HEADER_NAME));
    }

    private static void verifyBackendPostWithoutCookies(final int expectedCount, final UrlPattern urlPattern) {
        verify(expectedCount, postRequestedFor(urlPattern)
                .withoutHeader(org.springframework.http.HttpHeaders.COOKIE)
                .withoutHeader(XSRF_HEADER_NAME));
    }

    private static void verifyBackendGetNotCalled(final UrlPattern urlPattern) {
        verify(0, getRequestedFor(urlPattern));
    }

    private static void verifyBackendPostNotCalled(final UrlPattern urlPattern) {
        verify(0, postRequestedFor(urlPattern));
    }

    private WebTestClient.RequestHeadersSpec<?> createGet(final String uri) {
        return webTestClient.get().uri(uri);
    }

    private WebTestClient.RequestHeadersSpec<?> createPost(final String uri) {
        return webTestClient.post().uri(uri);
    }

    private WebTestClient.RequestHeadersSpec<?> withXsrf(final WebTestClient.RequestHeadersSpec<?> request) {
        return request
                .cookie(XSRF_COOKIE_NAME, XSRF_VALUE)
                .header(XSRF_HEADER_NAME, XSRF_VALUE);
    }

    private WebTestClient.RequestHeadersSpec<?> withJsonContentType(final WebTestClient.RequestHeadersSpec<?> request) {
        return request.header(org.springframework.http.HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON);
    }

    private WebTestClient.RequestHeadersSpec<?> createClientGet(final String uri) {
        return webTestClient.mutateWith(mockJwt())
                .get().uri(uri);
    }

    private WebTestClient.RequestHeadersSpec<?> createClientPost(final String uri) {
        return webTestClient.mutateWith(mockJwt())
                .post().uri(uri);
    }

}
