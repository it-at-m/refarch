package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileSizeDto;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentStorageFileRestRepository implements DocumentStorageFileRepository {

    private final PresignedUrlRepository presignedUrlRepository;

    private final S3FileTransferRepository s3FileTransferRepository;

    private final FileApiApi fileApi;

    @Override
    public byte[] getFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final Mono<String> presignedUrl = this.presignedUrlRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes);
        return this.s3FileTransferRepository.getFile(presignedUrl.block());
    }

    @Override
    public Mono<Long> getFileSize(final String pathToFile)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return fileApi.getFileSize(pathToFile).mapNotNull(FileSizeDto::getFileSize);
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

    @Override
    public InputStream getFileInputStream(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final Mono<String> presignedUrl = this.presignedUrlRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes);
        return this.s3FileTransferRepository.getFileInputStream(presignedUrl.block());
    }

    @Override
    public void saveFile(final String pathToFile, final byte[] file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.saveFile(presignedUrl, file);
    }

    @Override
    public void saveFileInputStream(final String pathToFile, final InputStream file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.saveFileInputStream(presignedUrl, file);
    }

    @Override
    public void updateFile(final String pathToFile, final byte[] file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.updateFile(presignedUrl, file);
    }

    @Override
    public void updateFileInputStream(final String pathToFile, final InputStream file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.updateFileInputStream(presignedUrl, file);
    }

    @Override
    public void deleteFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.deleteFile(presignedUrl);
    }

}
