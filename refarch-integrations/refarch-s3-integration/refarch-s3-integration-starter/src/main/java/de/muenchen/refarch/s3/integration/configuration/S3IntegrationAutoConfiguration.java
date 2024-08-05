package de.muenchen.refarch.s3.integration.configuration;

import de.muenchen.refarch.s3.integration.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.s3.integration.application.port.in.CreatePresignedUrlsInPort;
import de.muenchen.refarch.s3.integration.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.s3.integration.application.usecase.CreatePresignedUrlsUseCase;
import de.muenchen.refarch.s3.integration.application.usecase.FileOperationsPresignedUrlUseCase;
import de.muenchen.refarch.s3.integration.application.usecase.FileOperationsUseCase;
import de.muenchen.refarch.s3.integration.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.s3.integration.properties.S3IntegrationProperties;
import io.minio.MinioClient;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3IntegrationProperties.class)
public class S3IntegrationAutoConfiguration {

    public final S3IntegrationProperties s3IntegrationProperties;

    @ConditionalOnMissingBean
    @Bean
    public S3Adapter s3Adapter() throws FileSystemAccessException {
        final MinioClient minioClient = MinioClient.builder()
                .endpoint(this.s3IntegrationProperties.getUrl())
                .credentials(this.s3IntegrationProperties.getAccessKey(), this.s3IntegrationProperties.getSecretKey())
                .build();
        return new S3Adapter(
                this.s3IntegrationProperties.getBucketName(),
                this.s3IntegrationProperties.getUrl(),
                minioClient,
                BooleanUtils.isNotFalse(this.s3IntegrationProperties.getInitialConnectionTest()),
                this.s3IntegrationProperties.getProxyEnabled() ? Optional.of(this.s3IntegrationProperties.getProxyUrl()) : Optional.empty()
        );
    }

    @ConditionalOnMissingBean
    @Bean
    public CreatePresignedUrlsInPort createPresignedUrlsInPort(FileOperationsPresignedUrlUseCase fileHandlingService) {
        return new CreatePresignedUrlsUseCase(
                fileHandlingService,
                this.s3IntegrationProperties.getPresignedUrlExpiresInMinutes()
        );
    }

    @ConditionalOnMissingBean
    @Bean
    public FileOperationsInPort fileOperationsInPort(S3Adapter s3Adapter) {
        return new FileOperationsUseCase(s3Adapter);
    }

    @ConditionalOnMissingBean
    @Bean
    public FileOperationsPresignedUrlUseCase fileOperationsPresignedUrlUseCase(S3Adapter s3Adapter) {
        return new FileOperationsPresignedUrlUseCase(s3Adapter);
    }
}
