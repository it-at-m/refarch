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
public class DocumentStorageFolderRepository implements IDocumentStorageFolderRepository {
    private final FolderOperationsInPort folderOperationsInPort;

    @Override
    public void deleteFolder(String pathToFolder) throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            folderOperationsInPort.deleteFolder(pathToFolder);
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getAllFilesInFolderRecursively(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return folderOperationsInPort.getAllFilesInFolderRecursively(pathToFolder).pathToFiles();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Long> getAllFileSizesInFolderRecursively(String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            return folderOperationsInPort.getAllFileSizesInFolderRecursively(pathToFolder).fileSizes();
        } catch (FileSystemAccessException e) {
            throw new DocumentStorageException(e.getMessage(), e);
        }
    }
}
