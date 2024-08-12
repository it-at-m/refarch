package de.muenchen.refarch.integration.s3.client.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileSizesInFolderDto;
import de.muenchen.refarch.integration.s3.client.model.FilesInFolderDto;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class DocumentStorageFolderRestRepositoryTest {

    @Mock
    private FolderApiApi folderApi;

    private DocumentStorageFolderRestRepository documentStorageFolderRestRepository;

    @BeforeEach
    public void beforeEach() {
        this.documentStorageFolderRestRepository = new DocumentStorageFolderRestRepository(this.folderApi);
    }

    @Test
    void deleteFolder() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "folder";

        Mockito.reset(this.folderApi);
        when(this.folderApi.delete(pathToFolder)).thenReturn(Mono.empty());
        this.documentStorageFolderRestRepository.deleteFolder(pathToFolder);
        verify(this.folderApi, times(1)).delete(pathToFolder);

        Mockito.reset(this.folderApi);
        Mockito.doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(this.folderApi).delete(pathToFolder);
        assertThrows(DocumentStorageClientErrorException.class, () -> this.documentStorageFolderRestRepository.deleteFolder(pathToFolder));
        verify(this.folderApi, times(1)).delete(pathToFolder);

        Mockito.reset(this.folderApi);
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(this.folderApi).delete(pathToFolder);
        assertThrows(DocumentStorageServerErrorException.class, () -> this.documentStorageFolderRestRepository.deleteFolder(pathToFolder));
        verify(this.folderApi, times(1)).delete(pathToFolder);

        Mockito.reset(this.folderApi);
        Mockito.doThrow(new RestClientException("Something happened")).when(this.folderApi).delete(pathToFolder);
        assertThrows(DocumentStorageException.class, () -> this.documentStorageFolderRestRepository.deleteFolder(pathToFolder));
        verify(this.folderApi, times(1)).delete(pathToFolder);
    }

    @Test
    void getAllFilesInFolderRecursively() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "folder";

        final FilesInFolderDto filesInFolderDto = new FilesInFolderDto();
        filesInFolderDto.setPathToFiles(Set.of("folder/file.txt"));

        Mockito.reset(this.folderApi);
        when(this.folderApi.getAllFilesInFolderRecursively(pathToFolder)).thenReturn(Mono.just(filesInFolderDto));
        this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder);
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);

        Mockito.reset(this.folderApi);
        Mockito.doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(this.folderApi).getAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageClientErrorException.class, () -> this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);

        Mockito.reset(this.folderApi);
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(this.folderApi).getAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageServerErrorException.class, () -> this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);

        Mockito.reset(this.folderApi);
        Mockito.doThrow(new RestClientException("Something happened")).when(this.folderApi).getAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageException.class, () -> this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);
    }

    @Test
    void testGetAllFileSizesInFolderRecursively()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "path/to/folder";
        FileSizesInFolderDto fileSizesInFolderDto = new FileSizesInFolderDto();
        fileSizesInFolderDto.setFileSizes(Map.of("file1", 100L, "file2", 200L));
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Mono.just(fileSizesInFolderDto));

        Map<String, Long> result = documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder);
        assertEquals(Map.of("file1", 100L, "file2", 200L), result);
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);

        Mockito.reset(this.folderApi);
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenThrow(HttpClientErrorException.class);
        assertThrows(DocumentStorageClientErrorException.class, () -> documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);

        Mockito.reset(this.folderApi);
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenThrow(HttpServerErrorException.class);
        assertThrows(DocumentStorageServerErrorException.class, () -> documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);

        Mockito.reset(this.folderApi);
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenThrow(RestClientException.class);
        assertThrows(DocumentStorageException.class, () -> documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);
    }

}
