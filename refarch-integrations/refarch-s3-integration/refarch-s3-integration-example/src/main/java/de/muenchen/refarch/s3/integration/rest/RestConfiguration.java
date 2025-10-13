package de.muenchen.refarch.s3.integration.rest;

import de.muenchen.refarch.integration.s3.client.ApiClient;
import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRestRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRestRepository;
import de.muenchen.refarch.integration.s3.client.repository.mapper.FileMetadataMapper;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRestRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestConfiguration {

    @Bean
    public DocumentStorageFolderRestRepository documentStorageFolderRestRepository(
            final FolderApiApi folderApiApi,
            final FileMetadataMapper fileMetadataMapper) {
        return new DocumentStorageFolderRestRepository(folderApiApi, fileMetadataMapper);
    }

    @Bean
    public DocumentStorageFileRestRepository documentStorageFileRestRepository(
            final PresignedUrlRestRepository presignedUrlRestRepository,
            final S3FileTransferRepository s3FileTransferRepository,
            final FileApiApi fileApiApi) {
        return new DocumentStorageFileRestRepository(presignedUrlRestRepository, s3FileTransferRepository, fileApiApi);
    }

    @Bean
    public PresignedUrlRestRepository presignedUrlRestRepository(final FileApiApi fileApiApi) {
        return new PresignedUrlRestRepository(fileApiApi);
    }

    @Bean
    public FileApiApi fileApiApi(final ApiClient apiClient) {
        return new FileApiApi(apiClient);
    }

    @Bean
    public FolderApiApi folderApiApi(final ApiClient apiClient) {
        return new FolderApiApi(apiClient);
    }

    @Bean
    public ApiClient apiClient(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService,
            final RestProperties restProperties) {
        final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService));
        oauth.setDefaultClientRegistrationId("s3");
        final WebClient webClient = WebClient.builder()
                .apply(oauth.oauth2Configuration())
                .build();
        final ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(restProperties.getApiBaseUrl());
        return apiClient;
    }
}
