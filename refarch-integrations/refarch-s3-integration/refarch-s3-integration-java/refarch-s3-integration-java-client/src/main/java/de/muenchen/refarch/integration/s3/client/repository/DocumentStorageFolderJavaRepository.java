package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentStorageFolderJavaRepository implements DocumentStorageFolderRepository {
    private final FolderOperationsInPort folderOperationsInPort;

    @Override
    public void deleteFolder(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            folderOperationsInPort.deleteFolder(pathToFolder);
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getAllFilesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return folderOperationsInPort.getAllFilesInFolderRecursively(pathToFolder).pathToFiles();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Long> getAllFileSizesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return folderOperationsInPort.getAllFileSizesInFolderRecursively(pathToFolder).fileSizes();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }
}
