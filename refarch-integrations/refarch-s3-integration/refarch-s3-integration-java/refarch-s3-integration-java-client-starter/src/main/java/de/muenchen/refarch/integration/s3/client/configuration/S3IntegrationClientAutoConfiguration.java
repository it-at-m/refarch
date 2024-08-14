package de.muenchen.refarch.integration.s3.client.configuration;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import de.muenchen.refarch.integration.s3.client.properties.S3IntegrationClientProperties;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderRepository;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.integration.s3.client.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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

    /**
     * Instance of a {@link FileValidationService} containing externally given supported file
     * extensions.
     *
     * @param supportedFileExtensions {@link java.util.Map} of supported file extensions.
     * @return {@link FileValidationService} for managing file extensions.
     */
    @Bean
    @ConditionalOnBean(SupportedFileExtensions.class)
    public FileValidationService fileService(final SupportedFileExtensions supportedFileExtensions) {
        return new FileValidationService(supportedFileExtensions, this.s3IntegrationClientProperties.getMaxFileSize(),
                this.s3IntegrationClientProperties.getMaxBatchSize());
    }

    /**
     * Instance of a {@link FileValidationService} containing supported file extensions configured
     * within in the 'de.muenchen.refarch.s3' scope.
     *
     * @return {@link FileValidationService} for managing file extensions.
     */
    @Bean
    @ConditionalOnMissingBean(SupportedFileExtensions.class)
    public FileValidationService fileServiceFromS3IntegrationClientProperties() {
        return new FileValidationService(this.s3IntegrationClientProperties.getSupportedFileExtensions(), this.s3IntegrationClientProperties.getMaxFileSize(),
                this.s3IntegrationClientProperties.getMaxBatchSize());
    }

    @Bean
    @ConditionalOnMissingBean
    public PresignedUrlRepository presignedUrlRepository(
            final FileOperationsPresignedUrlInPort fileOperationsPresignedUrlInPort) {
        return new PresignedUrlJavaRepository(fileOperationsPresignedUrlInPort);
    }

    @Bean
    @ConditionalOnMissingBean
    public DocumentStorageFileRepository documentStorageFileRepository(
            final PresignedUrlRepository presignedUrlRepository,
            final S3FileTransferRepository s3FileTransferRepository,
            final FileOperationsInPort fileOperationsInPort) {
        return new DocumentStorageFileJavaRepository(presignedUrlRepository,
                s3FileTransferRepository, fileOperationsInPort);
    }

    @Bean
    @ConditionalOnBean
    public DocumentStorageFolderRepository documentStorageFolderRepository(
            final FolderOperationsInPort folderOperationsInPort) {
        return new DocumentStorageFolderJavaRepository(folderOperationsInPort);
    }
}
