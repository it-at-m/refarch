package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface DocumentStorageFolderRepository {
    /**
     * Deletes the folder with all containing files on document storage.
     *
     * @param pathToFolder which defines the folder in the document storage.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned directly to the document storage.
     */
    void deleteFolder(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Returns all files within a folder given in the parameter from document storage.
     *
     * @param pathToFolder which defines the folder in the document storage.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned directly to the document storage.
     */
    Mono<Set<String>> getAllFilesInFolderRecursively(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    /**
     * Returns all file sizes of files within a folder given in the parameter from document storage.
     *
     * @param pathToFolder defines the folder in the document storage.
     * @return file paths with their file sizes.
     * @throws DocumentStorageClientErrorException if the problem is with the client.
     * @throws DocumentStorageServerErrorException if the problem is with the document storage.
     * @throws DocumentStorageException            if the problem cannot be assigned directly to the document storage.
     */
    Mono<Map<String, Long>> getAllFileSizesInFolderRecursively(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;
}
