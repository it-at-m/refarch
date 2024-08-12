package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;

public class DocumentStorageFileJavaRepository extends DocumentStorageFileRepository {
    private final FileOperationsInPort fileOperationsInPort;

    public DocumentStorageFileJavaRepository(final PresignedUrlRepository presignedUrlRepository,
            final S3FileTransferRepository s3FileTransferRepository, final FileOperationsInPort fileOperationsInPort) {
        super(presignedUrlRepository, s3FileTransferRepository);
        this.fileOperationsInPort = fileOperationsInPort;
    }

    @Override
    public Long getFileSize(String pathToFile) throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileOperationsInPort.getFileSize(pathToFile).fileSize();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }
}
