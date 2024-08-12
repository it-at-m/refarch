package de.muenchen.refarch.integration.s3.client.repository;

import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileSizesInFolderDto;
import de.muenchen.refarch.integration.s3.client.model.FilesInFolderDto;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DocumentStorageFolderRepository implements IDocumentStorageFolderRepository {
    private final FolderApiApi folderApi;

    @Override
    public void deleteFolder(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            folderApi.delete(pathToFolder);
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
    public Mono<Set<String>> getAllFilesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<FilesInFolderDto> filesInFolderDto = folderApi.getAllFilesInFolderRecursively(pathToFolder);
            return filesInFolderDto.mapNotNull(FilesInFolderDto::getPathToFiles);
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
    public Mono<Map<String, Long>> getAllFileSizesInFolderRecursively(final String pathToFolder)
            throws DocumentStorageClientErrorException, DocumentStorageServerErrorException, DocumentStorageException {
        try {
            final Mono<FileSizesInFolderDto> fileSizesInFolderDtoMono = folderApi.getAllFileSizesInFolderRecursively(pathToFolder);
            return fileSizesInFolderDtoMono.mapNotNull(FileSizesInFolderDto::getFileSizes);
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
