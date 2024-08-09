package de.muenchen.refarch.integration.s3.client.repository.presignedurl;

import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileDataDto;
import de.muenchen.refarch.integration.s3.client.model.PresignedUrlDto;
import de.muenchen.refarch.integration.s3.client.service.ApiClientFactory;
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
public class PresignedUrlRepository {

    private final ApiClientFactory apiClientFactory;

    /**
     * Fetches a presignedURL for the file named in the parameter to get a file from the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @param documentStorageUrl to define to which document storage the request goes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    public Mono<String> getPresignedUrlGetFile(final String pathToFile, final int expireInMinutes, final String documentStorageUrl)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final FileApiApi fileApi = this.apiClientFactory.getFileApiForDocumentStorageUrl(documentStorageUrl);
            final Mono<PresignedUrlDto> presignedUrlDto = fileApi.get(pathToFile, expireInMinutes);
            return presignedUrlDto.mapNotNull(PresignedUrlDto::getUrl);
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

    /**
     * Fetches a presignedURL for the file named in the parameter to store a file in the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @param documentStorageUrl to define to which document storage the request goes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    public String getPresignedUrlSaveFile(final String pathToFile, final int expireInMinutes, final String documentStorageUrl)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final FileApiApi fileApi = this.apiClientFactory.getFileApiForDocumentStorageUrl(documentStorageUrl);
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

    /**
     * Fetches a presignedURL for the file named in the parameter to update a file in the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @param documentStorageUrl to define to which document storage the request goes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    public String getPresignedUrlUpdateFile(final String pathToFile, final int expireInMinutes,
            final String documentStorageUrl) throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final FileApiApi fileApi = this.apiClientFactory.getFileApiForDocumentStorageUrl(documentStorageUrl);
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

    /**
     * Fetches a presignedURL for the file named in the parameter to delete a file from the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @param documentStorageUrl to define to which document storage the request goes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    public String getPresignedUrlDeleteFile(final String pathToFile, final int expireInMinutes, final String documentStorageUrl)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final FileApiApi fileApi = this.apiClientFactory.getFileApiForDocumentStorageUrl(documentStorageUrl);
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
