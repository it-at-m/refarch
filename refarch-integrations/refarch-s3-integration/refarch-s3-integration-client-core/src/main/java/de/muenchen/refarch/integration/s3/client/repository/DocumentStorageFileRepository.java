package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import java.io.InputStream;
import reactor.core.publisher.Mono;

public interface DocumentStorageFileRepository {
    /**
     * Gets the file specified in the parameter from the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the file.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    byte[] getFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    /**
     * Retrieves the file size of a file specified in the parameter from the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @return the file size.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    Mono<Long> getFileSize(String pathToFile)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Gets an InputStream for the file specified in the parameter from the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the InputStream for the file.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    InputStream getFileInputStream(String pathToFile, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    /**
     * Saves the file specified in the parameter to the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param file            to save.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    void saveFile(String pathToFile, byte[] file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    /**
     * Saves the file specified in the parameter to the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param file            to save.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    void saveFileInputStream(String pathToFile, InputStream file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    /**
     * Updates the file specified in the parameter to the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param file            which overwrites the file in the document storage.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    void updateFile(String pathToFile, byte[] file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    /**
     * Updates the file specified in the parameter withinq the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param file            which overwrites the file in the document storage.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    void updateFileInputStream(String pathToFile, InputStream file, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;

    /**
     * Deletes the file specified in the parameter from the document storage.
     *
     * @param pathToFile      defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned to either the client or the S3 storage or the document storage.
     */
    void deleteFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException;
}
