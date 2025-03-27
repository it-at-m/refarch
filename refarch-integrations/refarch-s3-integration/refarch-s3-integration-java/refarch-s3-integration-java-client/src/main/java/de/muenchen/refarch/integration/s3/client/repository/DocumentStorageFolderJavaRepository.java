package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.client.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.repository.mapper.FileMetadataMapper;
import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.muenchen.refarch.integration.s3.domain.model.FilesMetadataInFolder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocumentStorageFolderJavaRepository implements DocumentStorageFolderRepository {
    private final FolderOperationsInPort folderOperationsInPort;
    private final FileMetadataMapper fileMetadataMapper;

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
    public List<FileMetadata> getMetadataOfAllFilesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final FilesMetadataInFolder filesMetadataInFolder = folderOperationsInPort.getMetadataOfAllFilesInFolderRecursively(pathToFolder);
            return fileMetadataMapper.map(filesMetadataInFolder.files());
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
