package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@Slf4j
@Repository
public class DocumentStorageFileRepository extends DocumentStorageFileRepository {
    private final FileApiApi fileApi;

    public DocumentStorageFileRepository(final PresignedUrlRepository presignedUrlRepository,
            final S3FileTransferRepository s3FileTransferRepository, final FileApiApi fileApi) {
        super(presignedUrlRepository, s3FileTransferRepository);
        this.fileApi = fileApi;
    }

    @Override
    public Long getFileSize(final String pathToFile)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileApi.getFileSize(pathToFile).block().getFileSize();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to get file size failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to get file size failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to get a file size failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }
}
