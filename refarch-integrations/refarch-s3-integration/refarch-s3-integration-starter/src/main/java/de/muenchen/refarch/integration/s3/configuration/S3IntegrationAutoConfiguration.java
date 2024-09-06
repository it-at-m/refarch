package de.muenchen.refarch.integration.s3.configuration;

import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.integration.s3.application.port.in.CreatePresignedUrlsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.application.usecase.CreatePresignedUrlsUseCase;
import de.muenchen.refarch.integration.s3.application.usecase.FileOperationsPresignedUrlUseCase;
import de.muenchen.refarch.integration.s3.application.usecase.FileOperationsUseCase;
import de.muenchen.refarch.integration.s3.application.usecase.FolderOperationsUseCase;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import de.muenchen.refarch.integration.s3.properties.S3IntegrationProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3IntegrationProperties.class)
@ComponentScan(basePackages = "de.muenchen.refarch.integration.s3")
public class S3IntegrationAutoConfiguration {

    public final S3IntegrationProperties s3IntegrationProperties;

    @ConditionalOnMissingBean
    @Bean
    public S3Adapter s3Adapter() throws FileSystemAccessException {
        final MinioClient minioClient = MinioClient.builder()
                .endpoint(this.s3IntegrationProperties.getUrl())
                .credentials(this.s3IntegrationProperties.getAccessKey(), this.s3IntegrationProperties.getSecretKey())
                .build();
        final S3Adapter adapter = new S3Adapter(
                this.s3IntegrationProperties.getBucketName(),
                minioClient);
        if (this.s3IntegrationProperties.getInitialConnectionTest()) {
            adapter.testConnection();
        }
        return adapter;
    }

    @ConditionalOnMissingBean
    @Bean
    public CreatePresignedUrlsInPort createPresignedUrlsInPort(final FileOperationsPresignedUrlUseCase fileHandlingService) {
        return new CreatePresignedUrlsUseCase(
                fileHandlingService,
                this.s3IntegrationProperties.getPresignedUrlExpiresInMinutes());
    }

    @ConditionalOnMissingBean
    @Bean
    public FileOperationsInPort fileOperationsInPort(final S3Adapter s3Adapter) {
        return new FileOperationsUseCase(s3Adapter);
    }

    @ConditionalOnMissingBean
    @Bean
    public FileOperationsPresignedUrlUseCase fileOperationsPresignedUrlUseCase(final S3Adapter s3Adapter) {
        return new FileOperationsPresignedUrlUseCase(s3Adapter);
    }

    @ConditionalOnMissingBean
    @Bean
    public FolderOperationsInPort folderOperationsInPort(final S3OutPort s3OutPort) {
        return new FolderOperationsUseCase(s3OutPort);
    }
}
