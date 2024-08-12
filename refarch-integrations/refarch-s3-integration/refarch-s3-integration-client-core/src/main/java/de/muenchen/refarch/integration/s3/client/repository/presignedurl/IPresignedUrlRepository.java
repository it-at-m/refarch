package de.muenchen.refarch.integration.s3.client.repository.presignedurl;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import reactor.core.publisher.Mono;

public interface IPresignedUrlRepository {
    Mono<String> getPresignedUrlGetFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    String getPresignedUrlSaveFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    String getPresignedUrlUpdateFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    String getPresignedUrlDeleteFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;
}
