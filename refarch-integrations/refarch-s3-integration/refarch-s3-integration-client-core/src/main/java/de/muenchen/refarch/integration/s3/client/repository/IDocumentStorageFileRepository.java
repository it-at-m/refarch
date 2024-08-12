package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import java.io.InputStream;
import reactor.core.publisher.Mono;

public interface IDocumentStorageFileRepository {
    byte[] getFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    Mono<Long> getFileSize(String pathToFile)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    InputStream getFileInputStream(String pathToFile, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    void saveFile(String pathToFile, byte[] file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    void saveFileInputStream(String pathToFile, InputStream file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    void updateFile(String pathToFile, byte[] file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    void updateFileInputStream(String pathToFile, InputStream file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    void deleteFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;
}
