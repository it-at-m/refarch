package de.muenchen.refarch.s3.integration.java;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsPresignedUrlInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFileJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.DocumentStorageFolderJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.mapper.FileMetadataMapper;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlJavaRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaConfiguration {
    @Bean
    public DocumentStorageFolderJavaRepository documentStorageFolderJavaRepository(
            final FolderOperationsInPort folderOperationsInPort,
            final FileMetadataMapper fileMetadataMapper) {
        return new DocumentStorageFolderJavaRepository(folderOperationsInPort, fileMetadataMapper);
    }

    @Bean
    public DocumentStorageFileJavaRepository documentStorageFileJavaRepository(
            final PresignedUrlJavaRepository presignedUrlJavaRepository,
            final S3FileTransferRepository s3FileTransferRepository,
            final FileOperationsInPort fileOperationsInPort) {
        return new DocumentStorageFileJavaRepository(presignedUrlJavaRepository, s3FileTransferRepository, fileOperationsInPort);
    }

    @Bean
    public PresignedUrlJavaRepository presignedUrlJavaRepository(final FileOperationsPresignedUrlInPort fileOperationsPresignedUrlInPort) {
        return new PresignedUrlJavaRepository(fileOperationsPresignedUrlInPort);
    }
}
