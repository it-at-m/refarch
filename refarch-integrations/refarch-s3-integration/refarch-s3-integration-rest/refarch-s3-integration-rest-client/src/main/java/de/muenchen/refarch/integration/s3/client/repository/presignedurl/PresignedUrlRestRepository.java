package de.muenchen.refarch.integration.s3.client.repository.presignedurl;

import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileDataDto;
import de.muenchen.refarch.integration.s3.client.model.PresignedUrlDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class PresignedUrlRestRepository implements PresignedUrlRepository {

    private final FileApiApi fileApi;

    @Override
    public String getPresignedUrlGetFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<PresignedUrlDto> presignedUrlDto = fileApi.get(pathToFile, expireInMinutes);
            return presignedUrlDto.block().getUrl();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to create a presigned url to get a file failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to create a presigned url to get a file failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to create a presigned url to get a file failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

    @Override
    public String getPresignedUrlSaveFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final var fileDataDto = new FileDataDto();
            fileDataDto.setPathToFile(pathToFile);
            fileDataDto.setExpiresInMinutes(expireInMinutes);
            final Mono<PresignedUrlDto> presignedUrlDto = fileApi.save(fileDataDto);
            return presignedUrlDto.block().getUrl();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to create a presigned save url failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to create a presigned save url failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to create a presigned save url failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

    @Override
    public String getPresignedUrlUpdateFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final var fileDataDto = new FileDataDto();
            fileDataDto.setPathToFile(pathToFile);
            fileDataDto.setExpiresInMinutes(expireInMinutes);
            final Mono<PresignedUrlDto> presignedUrlDto = fileApi.update(fileDataDto);
            return presignedUrlDto.block().getUrl();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to create a presigned update url failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to create a presigned update url failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to create a presigned update url failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

    @Override
    public String getPresignedUrlDeleteFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<PresignedUrlDto> presignedUrlDto = fileApi.delete1(pathToFile, expireInMinutes);
            return presignedUrlDto.block().getUrl();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to create a presigned url to delete a file failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to create a presigned url to delete a file failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to create a presigned url to delete a file failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

}
