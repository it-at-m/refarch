package de.muenchen.refarch.integration.s3.client.repository.presignedurl;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;

public interface PresignedUrlRepository {

    /**
     * Fetches a presignedURL for the file named in the parameter to get a file from the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    String getPresignedUrlGetFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Fetches a presignedURL for the file named in the parameter to store a file in the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    String getPresignedUrlSaveFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Fetches a presignedURL for the file named in the parameter to update a file in the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    String getPresignedUrlUpdateFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Fetches a presignedURL for the file named in the parameter to delete a file from the document
     * storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the presignedURL.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the
     *             document storage.
     */
    String getPresignedUrlDeleteFile(String pathToFile, int expireInMinutes)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;
}
