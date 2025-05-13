package de.muenchen.refarch.integration.s3.client.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.s3.client.api.FolderApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileMetadataDto;
import de.muenchen.refarch.integration.s3.client.model.FileSizesInFolderDto;
import de.muenchen.refarch.integration.s3.client.model.FilesInFolderDto;
import de.muenchen.refarch.integration.s3.client.model.FilesMetadataInFolderDto;
import de.muenchen.refarch.integration.s3.client.repository.mapper.FileMetadataMapperImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
        this.documentStorageFolderRestRepository = new DocumentStorageFolderRestRepository(
                this.folderApi,
                new FileMetadataMapperImpl());
    }

    @Test
    void deleteFolder() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "folder";

        reset(this.folderApi);
        when(this.folderApi.deleteFolder(pathToFolder)).thenReturn(Mono.empty());
        this.documentStorageFolderRestRepository.deleteFolder(pathToFolder);
        verify(this.folderApi, times(1)).deleteFolder(pathToFolder);

        reset(this.folderApi);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(this.folderApi).deleteFolder(pathToFolder);
        assertThrows(DocumentStorageClientErrorException.class, () -> this.documentStorageFolderRestRepository.deleteFolder(pathToFolder));
        verify(this.folderApi, times(1)).deleteFolder(pathToFolder);

        reset(this.folderApi);
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(this.folderApi).deleteFolder(pathToFolder);
        assertThrows(DocumentStorageServerErrorException.class, () -> this.documentStorageFolderRestRepository.deleteFolder(pathToFolder));
        verify(this.folderApi, times(1)).deleteFolder(pathToFolder);

        reset(this.folderApi);
        doThrow(new RestClientException("Something happened")).when(this.folderApi).deleteFolder(pathToFolder);
        assertThrows(DocumentStorageException.class, () -> this.documentStorageFolderRestRepository.deleteFolder(pathToFolder));
        verify(this.folderApi, times(1)).deleteFolder(pathToFolder);
    }

    @Test
    void testGetAllFilesInFolderRecursively() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "folder";

        final FilesInFolderDto filesInFolderDto = new FilesInFolderDto();
        filesInFolderDto.setPathToFiles(Set.of("folder/file.txt"));

        reset(this.folderApi);
        when(this.folderApi.getAllFilesInFolderRecursively(pathToFolder)).thenReturn(Mono.just(filesInFolderDto));
        this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder);
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(this.folderApi).getAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageClientErrorException.class, () -> this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(this.folderApi).getAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageServerErrorException.class, () -> this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        doThrow(new RestClientException("Something happened")).when(this.folderApi).getAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageException.class, () -> this.documentStorageFolderRestRepository.getAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFilesInFolderRecursively(pathToFolder);
    }

    @Test
    void testGetMetadataOfAllFilesInFolderRecursively()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "folder";

        final FilesMetadataInFolderDto filesMetadataInFolderDto = new FilesMetadataInFolderDto();

        final List<FileMetadataDto> files = new ArrayList<>();
        FileMetadataDto fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setFileSize(1001L);
        fileMetadataDto.setPathToFile("/file1");
        fileMetadataDto.setEtag("etag1");
        files.add(fileMetadataDto);
        fileMetadataDto.setLastModified(LocalDateTime.of(2025, 2, 1, 23, 59, 59));
        fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setFileSize(1002L);
        fileMetadataDto.setPathToFile("/file2");
        fileMetadataDto.setEtag("etag2");
        fileMetadataDto.setLastModified(LocalDateTime.of(2025, 2, 2, 23, 59, 59));
        files.add(fileMetadataDto);

        filesMetadataInFolderDto.setFiles(files);

        reset(this.folderApi);
        when(this.folderApi.getMetadataOfAllFilesInFolderRecursively(pathToFolder)).thenReturn(Mono.just(filesMetadataInFolderDto));
        this.documentStorageFolderRestRepository.getMetadataOfAllFilesInFolderRecursively(pathToFolder);
        verify(this.folderApi, times(1)).getMetadataOfAllFilesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST)).when(this.folderApi).getMetadataOfAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageClientErrorException.class,
                () -> this.documentStorageFolderRestRepository.getMetadataOfAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getMetadataOfAllFilesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(this.folderApi).getMetadataOfAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageServerErrorException.class,
                () -> this.documentStorageFolderRestRepository.getMetadataOfAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getMetadataOfAllFilesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        doThrow(new RestClientException("Something happened")).when(this.folderApi).getMetadataOfAllFilesInFolderRecursively(pathToFolder);
        assertThrows(DocumentStorageException.class, () -> this.documentStorageFolderRestRepository.getMetadataOfAllFilesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getMetadataOfAllFilesInFolderRecursively(pathToFolder);
    }

    @Test
    void testGetAllFileSizesInFolderRecursively()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFolder = "path/to/folder";
        final FileSizesInFolderDto fileSizesInFolderDto = new FileSizesInFolderDto();
        fileSizesInFolderDto.setFileSizes(Map.of("file1", 100L, "file2", 200L));
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenReturn(Mono.just(fileSizesInFolderDto));

        final Map<String, Long> result = documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder);
        assertEquals(Map.of("file1", 100L, "file2", 200L), result);
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenThrow(HttpClientErrorException.class);
        assertThrows(DocumentStorageClientErrorException.class, () -> documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenThrow(HttpServerErrorException.class);
        assertThrows(DocumentStorageServerErrorException.class, () -> documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);

        reset(this.folderApi);
        when(folderApi.getAllFileSizesInFolderRecursively(anyString())).thenThrow(RestClientException.class);
        assertThrows(DocumentStorageException.class, () -> documentStorageFolderRestRepository.getAllFileSizesInFolderRecursively(pathToFolder));
        verify(this.folderApi, times(1)).getAllFileSizesInFolderRecursively(pathToFolder);
    }

}
