package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;

public interface IDocumentStorageFolderRepository {
    void deleteFolder(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    Mono<Set<String>> getAllFilesInFolderRecursively(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;

    Mono<Map<String, Long>> getAllFileSizesInFolderRecursively(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException;
}
