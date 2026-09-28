package de.muenchen.oss.refarch.integration.s3.configuration;

import de.muenchen.oss.refarch.integration.s3.adapter.out.s3.S3ListHelper;
import de.muenchen.oss.refarch.integration.s3.adapter.out.s3.S3Mapper;
import de.muenchen.oss.refarch.integration.s3.adapter.out.s3.S3OutAdapter;
import de.muenchen.oss.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.oss.refarch.integration.s3.properties.S3IntegrationProperties;
import java.net.URI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3IntegrationProperties.class)
@ComponentScan(basePackages = "de.muenchen.oss.refarch.integration.s3")
@Slf4j
public class S3IntegrationAutoConfiguration {

    private final S3IntegrationProperties s3IntegrationProperties;

    @Bean
    @ConditionalOnMissingBean
    protected S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(s3IntegrationProperties.isPathStyleAccessEnabled())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    protected SdkHttpClient.Builder<?> s3HttpClientBuilder() {
        return ApacheHttpClient.builder()
                .connectionTimeout(s3IntegrationProperties.getConnectionTimeout())
                .socketTimeout(s3IntegrationProperties.getSocketTimeout());
    }

    @Bean
    @ConditionalOnMissingBean
    protected StaticCredentialsProvider s3Cred() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        s3IntegrationProperties.getAccessKey(),
                        s3IntegrationProperties.getSecretKey()));
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(final S3Configuration s3Configuration, final SdkHttpClient.Builder<?> s3HttpClientBuilder,
            final StaticCredentialsProvider s3Cred) {
        final S3Client client = S3Client.builder()
                .httpClientBuilder(s3HttpClientBuilder)
                .endpointOverride(URI.create(s3IntegrationProperties.getUrl()))
                .region(Region.of(s3IntegrationProperties.getRegion()))
                .credentialsProvider(s3Cred)
                .serviceConfiguration(s3Configuration)
                .build();

        if (s3IntegrationProperties.isInitialConnectionTest()) {
            try {
                client.listBuckets();
                if (log.isInfoEnabled()) {
                    log.info("Initial S3 connection test succeeded for endpoint {}", s3IntegrationProperties.getUrl());
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Initial S3 connection test failed for endpoint " + s3IntegrationProperties.getUrl(), e);
            }
        }

        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Presigner s3Presigner(final S3Configuration s3Configuration, final StaticCredentialsProvider s3Cred) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(s3IntegrationProperties.getUrl()))
                .region(Region.of(s3IntegrationProperties.getRegion()))
                .credentialsProvider(s3Cred)
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Mapper s3Mapper() {
        return new S3Mapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3ListHelper s3ListHelper(final S3Client s3Client, final S3Mapper s3Mapper) {
        return new S3ListHelper(s3Client, s3Mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public S3OutPort s3Adapter(final S3Mapper s3Mapper, final S3Client s3Client, final S3Presigner s3Presigner,
            final S3ListHelper s3ListHelper) {
        return new S3OutAdapter(s3Mapper, s3Client, s3Presigner, s3ListHelper);
    }
}
