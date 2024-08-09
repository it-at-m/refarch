package de.muenchen.refarch.integration.s3.client.configuration;

import de.muenchen.refarch.integration.s3.client.ApiClient;
import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import de.muenchen.refarch.integration.s3.client.properties.S3IntegrationClientProperties;
import de.muenchen.refarch.integration.s3.client.service.ApiClientFactory;
import de.muenchen.refarch.integration.s3.client.service.FileService;
import de.muenchen.refarch.integration.s3.client.service.S3DomainProvider;
import de.muenchen.refarch.integration.s3.client.service.S3StorageUrlProvider;
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
import org.springframework.context.annotation.FilterType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ComponentScan(
        basePackages = {
                "de.muenchen.refarch.integration.s3.client"
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                /*
                                 * Exclude to avoid multiple instantiation of multiple beans with same name.
                                 * This class is instantiated in {@link S3IntegrationClientAutoConfiguration}
                                 * to give the bean another name.
                                 */
                                ApiClient.class,
                                FileApiApi.class,
                                FolderApiApi.class
                        }
                )
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
    public ApiClientFactory securedApiClientFactory(final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService) {
        return new ApiClientFactory(
                this.webClient(clientRegistrationRepository, authorizedClientService));
    }

    @Bean
    @ConditionalOnProperty(prefix = "refarch.s3.client", name = "enable-security", havingValue = "false", matchIfMissing = true)
    public ApiClientFactory apiClientFactory() {
        return new ApiClientFactory(
                WebClient.builder().build());
    }

    private WebClient webClient(
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
     * Instance of a {@link FileService} containing supported file extensions configured within in the
     * 'de.muenchen.oss.digiwf.s3' scope.
     *
     * @return {@link FileService} for managing file extensions.
     */
    @Bean
    @ConditionalOnMissingBean(SupportedFileExtensions.class)
    public FileService fileServiceFromS3IntegrationClientProperties() {
        return new FileService(this.s3IntegrationClientProperties.getSupportedFileExtensions(), this.s3IntegrationClientProperties.getMaxFileSize(),
                this.s3IntegrationClientProperties.getMaxBatchSize());
    }

    /**
     * Instance of an {@link S3StorageUrlProvider} containing an externally created
     * {@link S3DomainProvider} for retrieving the S3 storage URL.
     *
     * @param s3DomainProvider Provider of domain specific S3 storages configured in process
     *            configurations.
     * @return Provider of the S3 storage URL.
     */
    @Bean
    @ConditionalOnBean(S3DomainProvider.class)
    public S3StorageUrlProvider s3StorageUrlProvider(final S3DomainProvider s3DomainProvider) {
        return new S3StorageUrlProvider(s3DomainProvider, this.s3IntegrationClientProperties.getDocumentStorageUrl());
    }

    /**
     * Instance of an {@link S3StorageUrlProvider} containing a default {@link S3DomainProvider}. The
     * instance will only return the default S3 URL.
     *
     * @return Provider of the S3 storage URL.
     */
    @Bean
    @ConditionalOnMissingBean(S3DomainProvider.class)
    public S3StorageUrlProvider s3StorageUrlProviderWithoutDomainProvider() {
        return new S3StorageUrlProvider(this.s3IntegrationClientProperties.getDocumentStorageUrl());
    }

}
