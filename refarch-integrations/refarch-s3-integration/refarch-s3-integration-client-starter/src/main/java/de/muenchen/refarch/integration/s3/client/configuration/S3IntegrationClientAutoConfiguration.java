package de.muenchen.refarch.integration.s3.client.configuration;

import de.muenchen.refarch.integration.s3.client.ApiClient;
import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import de.muenchen.refarch.integration.s3.client.properties.S3IntegrationClientProperties;
import de.muenchen.refarch.integration.s3.client.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ComponentScan(
        basePackages = {
                "de.muenchen.refarch.integration.s3.client"
        }
)
@RequiredArgsConstructor
@EnableConfigurationProperties(S3IntegrationClientProperties.class)
@Slf4j
public class S3IntegrationClientAutoConfiguration {

    public final S3IntegrationClientProperties s3IntegrationClientProperties;

    @PostConstruct
    public void init() {
        log.info("[REFARCH-S3-INTEGRATION-CLIENT]: Staring integration client, security is {}.",
                s3IntegrationClientProperties.isEnableSecurity() ? "enabled" : "disabled");
    }

    @Bean
    @ConditionalOnProperty(prefix = "refarch.s3.client", name = "enable-security", havingValue = "true")
    public ApiClient securedApiClientFactory(final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        return new ApiClient(
                this.authenticatedWebClient(clientRegistrationRepository, authorizedClientService));
    }

    @Bean
    @ConditionalOnProperty(prefix = "refarch.s3.client", name = "enable-security", havingValue = "false", matchIfMissing = true)
    public ApiClient apiClientFactory() {
        return new ApiClient(
                WebClient.builder().build());
    }

    private WebClient authenticatedWebClient(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        final var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService));
        oauth.setDefaultClientRegistrationId("s3");
        return WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .build();
    }

    /**
     * Instance of a {@link FileService} containing externally given supported file extensions.
     *
     * @param supportedFileExtensions {@link java.util.Map} of supported file extensions.
     * @return {@link FileService} for managing file extensions.
     */
    @Bean
    @ConditionalOnBean(SupportedFileExtensions.class)
    public FileService fileService(final SupportedFileExtensions supportedFileExtensions) {
        return new FileService(supportedFileExtensions, this.s3IntegrationClientProperties.getMaxFileSize(),
                this.s3IntegrationClientProperties.getMaxBatchSize());
    }

    /**
     * Instance of a {@link FileService} containing supported file extensions configured within in the 'de.muenchen.refarch.s3' scope.
     *
     * @return {@link FileService} for managing file extensions.
     */
    @Bean
    @ConditionalOnMissingBean(SupportedFileExtensions.class)
    public FileService fileServiceFromS3IntegrationClientProperties() {
        return new FileService(this.s3IntegrationClientProperties.getSupportedFileExtensions(), this.s3IntegrationClientProperties.getMaxFileSize(),
                this.s3IntegrationClientProperties.getMaxBatchSize());
    }

    @Bean
    @ConditionalOnMissingBean
    public FileApiApi fileApiApi(final ApiClient apiClient) {
        return new FileApiApi(apiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public FolderApiApi folderApiApi(final ApiClient apiClient) {
        return new FolderApiApi(apiClient);
    }
}
