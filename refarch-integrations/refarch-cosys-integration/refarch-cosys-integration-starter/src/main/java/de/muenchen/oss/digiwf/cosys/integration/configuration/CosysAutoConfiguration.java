package de.muenchen.oss.digiwf.cosys.integration.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.oss.digiwf.cosys.integration.ApiClient;
import de.muenchen.oss.digiwf.cosys.integration.adapter.in.streaming.GenerateDocumentDTO;
import de.muenchen.oss.digiwf.cosys.integration.adapter.in.streaming.GenerateDocumentPresignedUrlsDTO;
import de.muenchen.oss.digiwf.cosys.integration.adapter.in.streaming.StreamingAdapter;
import de.muenchen.oss.digiwf.cosys.integration.adapter.out.cosys.CosysAdapter;
import de.muenchen.oss.digiwf.cosys.integration.adapter.out.s3.S3Adapter;
import de.muenchen.oss.digiwf.cosys.integration.api.GenerationApi;
import de.muenchen.oss.digiwf.cosys.integration.application.port.in.CreateDocumentInPort;
import de.muenchen.oss.digiwf.cosys.integration.application.port.out.GenerateDocumentOutPort;
import de.muenchen.oss.digiwf.cosys.integration.application.port.out.SaveFileToStorageOutPort;
import de.muenchen.oss.digiwf.cosys.integration.application.usecase.CreateDocumentUseCase;
import de.muenchen.oss.digiwf.message.process.api.ErrorApi;
import de.muenchen.oss.digiwf.message.process.api.ProcessApi;
import de.muenchen.oss.digiwf.s3.integration.client.configuration.S3IntegrationClientAutoConfiguration;
import de.muenchen.oss.digiwf.s3.integration.client.repository.DocumentStorageFileRepository;
import de.muenchen.oss.digiwf.s3.integration.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.oss.digiwf.s3.integration.client.service.FileService;
import de.muenchen.oss.digiwf.s3.integration.client.service.S3StorageUrlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter({S3IntegrationClientAutoConfiguration.class})
@ComponentScan(basePackages = {"de.muenchen.oss.digiwf.cosys.integration"})
@EnableConfigurationProperties({CosysProperties.class})
public class CosysAutoConfiguration {

    private final CosysProperties cosysProperties;

    @Bean
    public CosysConfiguration cosysConfiguration() throws JsonProcessingException {
        final CosysConfiguration cosysConfiguration = new CosysConfiguration();
        cosysConfiguration.setUrl(this.cosysProperties.getUrl());

        final Map<String, String> mergeOptions = new HashMap<>();
        mergeOptions.put("--input-language", this.cosysProperties.getMerge().getInputLanguage());
        mergeOptions.put("--output-language", this.cosysProperties.getMerge().getOutputLanguage());
        mergeOptions.put("--keep-fields", this.cosysProperties.getMerge().getKeepFields());
        mergeOptions.put("-@1", "");

        final ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(mergeOptions);

        cosysConfiguration.setMergeOptions(json.getBytes());

        return cosysConfiguration;
    }

    @Bean
    public GenerationApi generationApi(final ApiClient apiClient) {
        return new GenerationApi(apiClient);
    }

    @Bean
    public ApiClient cosysApiClient(final ClientRegistrationRepository clientRegistrationRepository,
                                    final OAuth2AuthorizedClientService authorizedClientService) {
        final ApiClient apiClient = new ApiClient(this.webClient(clientRegistrationRepository, authorizedClientService));
        apiClient.setBasePath(this.cosysProperties.getUrl());
        return apiClient;
    }

    private WebClient webClient(
            final ClientRegistrationRepository clientRegistrationRepository,
            final OAuth2AuthorizedClientService authorizedClientService
    ) {
        final var oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService
                )
        );
        oauth.setDefaultClientRegistrationId("cosys");
        return WebClient.builder()
                .baseUrl(this.cosysProperties.getUrl())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(32 * 1024 * 1024))
                        .build())
                .apply(oauth.oauth2Configuration())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CreateDocumentInPort getCreateDocumentInPort(final SaveFileToStorageOutPort saveFileToStorageOutPort, final GenerateDocumentOutPort generateDocumentOutPort) {
        return new CreateDocumentUseCase(saveFileToStorageOutPort, generateDocumentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public SaveFileToStorageOutPort getSaveFileToStorageOutPort(
            final S3FileTransferRepository s3FileTransferRepository,
            final DocumentStorageFileRepository documentStorageFileRepository,
            final FileService fileService,
            final S3StorageUrlProvider s3DomainService) {
        return new S3Adapter(s3FileTransferRepository, documentStorageFileRepository, fileService, s3DomainService);
    }

    @Bean
    @ConditionalOnMissingBean
    public GenerateDocumentOutPort getGenerateDocumentOutPort(final CosysConfiguration cosysConfiguration, final GenerationApi generationApi) {
        return new CosysAdapter(cosysConfiguration, generationApi);
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamingAdapter streamingAdapter(final CreateDocumentInPort createDocumentInPort, final ProcessApi processApi, final ErrorApi errorApi) {
        return new StreamingAdapter(createDocumentInPort, processApi, errorApi);
    }

    @Bean
    public Consumer<Message<GenerateDocumentPresignedUrlsDTO>> createCosysDocument(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.createCosysDocument();
    }

    @Bean
    public Consumer<Message<GenerateDocumentDTO>> createCosysDocumentV2(final StreamingAdapter streamingAdapter) {
        return streamingAdapter.createCosysDocumentV2();
    }
}
