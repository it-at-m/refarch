package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class DocumentStorageFileRepository {
    protected final PresignedUrlRepository presignedUrlRepository;
    protected final S3FileTransferRepository s3FileTransferRepository;

    /**
     * Gets the file specified in the parameter from the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the file.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public byte[] getFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes);
        return this.s3FileTransferRepository.getFile(presignedUrl);
    }

    /**
     * Retrieves the file size of a file specified in the parameter from the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @return the file size.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public abstract Long getFileSize(String pathToFile)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Gets an InputStream for the file specified in the parameter from the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @return the InputStream for the file.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public InputStream getFileInputStream(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes);
        return this.s3FileTransferRepository.getFileInputStream(presignedUrl);
    }

    /**
     * Saves the file specified in the parameter to the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param file to save.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public void saveFile(final String pathToFile, final byte[] file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.saveFile(presignedUrl, file);
    }

    /**
     * Saves the file specified in the parameter to the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param file to save.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public void saveFileInputStream(final String pathToFile, final InputStream file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.saveFileInputStream(presignedUrl, file);
    }

    /**
     * Updates the file specified in the parameter to the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param file which overwrites the file in the document storage.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public void updateFile(final String pathToFile, final byte[] file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.updateFile(presignedUrl, file);
    }

    /**
     * Updates the file specified in the parameter withinq the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param file which overwrites the file in the document storage.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public void updateFileInputStream(final String pathToFile, final InputStream file, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.updateFileInputStream(presignedUrl, file);
    }

    /**
     * Deletes the file specified in the parameter from the document storage.
     *
     * @param pathToFile defines the path to the file.
     * @param expireInMinutes the expiration time of the presignedURL in minutes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the S3 storage or document
     *             storage.
     * @throws DocumentStorageException if the problem cannot be assigned to either the client or the S3
     *             storage or the document storage.
     */
    public void deleteFile(final String pathToFile, final int expireInMinutes)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String presignedUrl = this.presignedUrlRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes);
        this.s3FileTransferRepository.deleteFile(presignedUrl);
    }
}
