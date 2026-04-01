package de.muenchen.oss.refarch.integration.address.configuration;

import de.muenchen.oss.refarch.integration.address.adapter.out.address.AddressOutAdapter;
import de.muenchen.oss.refarch.integration.address.application.port.out.AddressOutPort;
import de.muenchen.oss.refarch.integration.address.client.api.AddressGermanyApi;
import de.muenchen.oss.refarch.integration.address.client.api.AddressMunichApi;
import de.muenchen.oss.refarch.integration.address.client.api.StreetsMunichApi;
import de.muenchen.oss.refarch.integration.address.client.gen.ApiClient;
import de.muenchen.oss.refarch.integration.address.client.gen.api.AdressenBundesweitApi;
import de.muenchen.oss.refarch.integration.address.client.gen.api.AdressenMnchenApi;
import de.muenchen.oss.refarch.integration.address.client.gen.api.StraenMnchenApi;
import de.muenchen.oss.refarch.integration.address.client.impl.AddressGermanyImpl;
import de.muenchen.oss.refarch.integration.address.client.impl.AddressesMunichImpl;
import de.muenchen.oss.refarch.integration.address.client.impl.StreetsMunichImpl;
import de.muenchen.oss.refarch.integration.address.properties.AddressServiceIntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@ComponentScan(
        basePackages = "de.muenchen.oss.refarch.integration.address"
)
@EnableConfigurationProperties(AddressServiceIntegrationProperties.class)
public class AddressServiceIntegrationAutoConfiguration {

    public final AddressServiceIntegrationProperties addressServiceIntegrationProperties;

    /**
     * Provides a correct configured {@link ApiClient}.
     *
     * @return a configured {@link ApiClient}.
     */
    public ApiClient addressServiceApiClient() {
        final ApiClient client = new ApiClient(WebClient.create(addressServiceIntegrationProperties.getUrl()));
        client.setBasePath(addressServiceIntegrationProperties.getUrl());
        return client;
    }

    /**
     * Create the bean manually to use the correct configured {@link ApiClient}.
     *
     * @return a bean of type {@link AdressenBundesweitApi} named by method name.
     */
    @Bean
    public AdressenBundesweitApi addressServiceAdressenBundesweitApi() {
        final ApiClient apiClient = this.addressServiceApiClient();
        return new AdressenBundesweitApi(apiClient);
    }

    /**
     * Create the bean manually to use the correct configured {@link ApiClient}.
     *
     * @return a bean of type {@link AdressenMnchenApi} named by method name.
     */
    @Bean
    public AdressenMnchenApi addressServiceAdressenMnchenApi() {
        final ApiClient apiClient = this.addressServiceApiClient();
        return new AdressenMnchenApi(apiClient);
    }

    /**
     * Create the bean manually to use the correct configured {@link ApiClient}.
     *
     * @return a bean of type {@link StraenMnchenApi} named by method name.
     */
    @Bean
    public StraenMnchenApi addressServiceStraenMnchenApi() {
        final ApiClient apiClient = this.addressServiceApiClient();
        return new StraenMnchenApi(apiClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public AddressOutPort addressOutPort(
            final AddressGermanyApi addressGermanyApi,
            final AddressMunichApi addressMunichApi,
            final StreetsMunichApi streetsMunichApi) {
        return new AddressOutAdapter(addressGermanyApi, addressMunichApi, streetsMunichApi);
    }

    @ConditionalOnMissingBean
    @Bean
    public AddressGermanyApi addressGermanyApi(final AdressenBundesweitApi apiClient) {
        return new AddressGermanyImpl(apiClient);
    }

    @ConditionalOnMissingBean
    @Bean
    public AddressMunichApi addressMunichApi(final AdressenMnchenApi apiClient) {
        return new AddressesMunichImpl(apiClient);
    }

    @ConditionalOnMissingBean
    @Bean
    public StreetsMunichApi munichStreetApi(final StraenMnchenApi apiClient) {
        return new StreetsMunichImpl(apiClient);
    }

}
