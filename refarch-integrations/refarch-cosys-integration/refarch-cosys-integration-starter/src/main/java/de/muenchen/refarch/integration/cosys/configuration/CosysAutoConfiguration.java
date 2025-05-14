package de.muenchen.refarch.integration.cosys.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.muenchen.refarch.integration.cosys.ApiClient;
import de.muenchen.refarch.integration.cosys.adapter.out.cosys.CosysAdapter;
import de.muenchen.refarch.integration.cosys.api.GenerationApi;
import de.muenchen.refarch.integration.cosys.application.port.in.CreateDocumentInPort;
import de.muenchen.refarch.integration.cosys.application.port.out.GenerateDocumentOutPort;
import de.muenchen.refarch.integration.cosys.application.usecase.CreateDocumentUseCase;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = { "de.muenchen.refarch.integration.cosys" })
@EnableConfigurationProperties({ CosysProperties.class })
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
            final OAuth2AuthorizedClientService authorizedClientService) {
        final ServletOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService));
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
    public CreateDocumentInPort getCreateDocumentInPort(final GenerateDocumentOutPort generateDocumentOutPort) {
        return new CreateDocumentUseCase(generateDocumentOutPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public GenerateDocumentOutPort getGenerateDocumentOutPort(final CosysConfiguration cosysConfiguration, final GenerationApi generationApi) {
        return new CosysAdapter(cosysConfiguration, generationApi);
    }
}
