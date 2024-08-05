package de.muenchen.refarch.spring.security.client;

import static java.util.Arrays.asList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnProperty(value = "refarch.security.service-account", matchIfMissing = true)
public class ServiceAccountAccessTokenConfiguration {

    private final static int TIMEOUT = 20_000;

    private static RestTemplate constructRestTemplate() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        final RestTemplate restTemplate = new RestTemplate(asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager serviceAccountAuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {
        // We need to create and configure all these objects manually in order to set the timeouts.
        // These are the objects that would be created if we just used a `new ClientCredentialsOAuth2AuthorizedClientProvider()` with only the timeouts added.
        final RestTemplate restTemplate = constructRestTemplate();

        final DefaultClientCredentialsTokenResponseClient accessTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        accessTokenResponseClient.setRestOperations(restTemplate);

        final ClientCredentialsOAuth2AuthorizedClientProvider authorizedClientProvider = new ClientCredentialsOAuth2AuthorizedClientProvider();
        authorizedClientProvider.setAccessTokenResponseClient(accessTokenResponseClient);

        final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

}
