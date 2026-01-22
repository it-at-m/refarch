package de.muenchen.refarch.integration.s3.configuration;

import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Mapper;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.application.usecase.FileOperationsUseCase;
import de.muenchen.refarch.integration.s3.application.usecase.FolderOperationsUseCase;
import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.integration.s3.properties.S3IntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3IntegrationProperties.class)
@ComponentScan(basePackages = "de.muenchen.refarch.integration.s3")
public class S3IntegrationAutoConfiguration {

    private final S3IntegrationProperties s3IntegrationProperties;

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client() {
        final S3Configuration cfg = S3Configuration.builder()
                .pathStyleAccessEnabled(s3IntegrationProperties.getPathStyleAccessEnabled())
                .build();

        final StaticCredentialsProvider creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        s3IntegrationProperties.getAccessKey(),
                        s3IntegrationProperties.getSecretKey()
                ));

        return S3Client.builder()
                .endpointOverride(URI.create(s3IntegrationProperties.getUrl()))
                .credentialsProvider(creds)
                .serviceConfiguration(cfg)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Presigner s3Presigner() {
        final StaticCredentialsProvider creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        s3IntegrationProperties.getAccessKey(),
                        s3IntegrationProperties.getSecretKey()
                ));

        return S3Presigner.builder()
                .endpointOverride(URI.create(s3IntegrationProperties.getUrl()))
                .credentialsProvider(creds)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Mapper s3Mapper() {
        return new S3Mapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Adapter s3Adapter(final S3Mapper s3Mapper, final S3Client s3Client, final S3Presigner s3Presigner) {
        return new S3Adapter(s3Mapper, s3Client, s3Presigner);
    }

    @Bean
    @ConditionalOnMissingBean
    public FileOperationsInPort fileOperationsInPort(final S3Adapter s3Adapter) {
        return new FileOperationsUseCase(s3Adapter);
    }

    @Bean
    @ConditionalOnMissingBean
    public FolderOperationsInPort folderOperationsInPort(final S3OutPort s3OutPort) {
        return new FolderOperationsUseCase(s3OutPort);
    }
}