package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileSizesInFolderDto;
import de.muenchen.refarch.integration.s3.client.model.FilesInFolderDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.muenchen.refarch.integration.s3.client.model.FilesMetadataInFolderDto;
import de.muenchen.refarch.integration.s3.client.repository.mapper.FileMetadataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class DocumentStorageFolderRestRepository implements DocumentStorageFolderRepository {

    private final FolderApiApi folderApi;
    private final FileMetadataMapper fileMetadataMapper;

    @Override
    public void deleteFolder(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            folderApi.delete1(pathToFolder).block();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to delete a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to delete a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to delete a folder failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

    @Override
    public Set<String> getAllFilesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<FilesInFolderDto> filesInFolderDto = folderApi.getAllFilesInFolderRecursively(pathToFolder);
            return filesInFolderDto.block().getPathToFiles();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to get all files within a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to get all files within a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to get all files within a folder failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

    @Override
    public List<FileMetadata> getMetadataOfAllFilesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<FilesMetadataInFolderDto> filesMetadateInFolder = folderApi.getMetadataOfAllFilesInFolderRecursively(pathToFolder);
            return fileMetadataMapper.dto2Model(filesMetadateInFolder.block().getFiles());
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to get the metadata of all files within a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to get the metadata of all files within a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to get the metadata of all files within a folder failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

    @Override
    public Map<String, Long> getAllFileSizesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<FileSizesInFolderDto> fileSizesInFolderDtoMono = folderApi.getAllFileSizesInFolderRecursively(pathToFolder);
            return fileSizesInFolderDtoMono.block().getFileSizes();
        } catch (final HttpClientErrorException exception) {
            final String message = String.format("The request to get all file sizes within a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageClientErrorException(message, exception);
        } catch (final HttpServerErrorException exception) {
            final String message = String.format("The request to get all file sizes within a folder failed %s.", exception.getStatusCode());
            log.error(message);
            throw new DocumentStorageServerErrorException(message, exception);
        } catch (final RestClientException exception) {
            final String message = "The request to get all file sizes within a folder failed.";
            log.error(message);
            throw new DocumentStorageException(message, exception);
        }
    }

}
