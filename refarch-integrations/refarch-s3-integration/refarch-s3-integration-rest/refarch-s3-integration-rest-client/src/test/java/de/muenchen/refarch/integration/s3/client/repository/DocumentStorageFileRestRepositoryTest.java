package de.muenchen.refarch.integration.s3.client.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileSizeDto;
import de.muenchen.refarch.integration.s3.client.repository.presignedurl.PresignedUrlRestRepository;
import de.muenchen.refarch.integration.s3.client.repository.transfer.S3FileTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class DocumentStorageFileRestRepositoryTest {

    public static final String PATH_TO_FILE = "folder/file.txt";
    public static final String PRESIGNED_URL = "the_presignedUrl";
    @Mock
    private PresignedUrlRestRepository presignedUrlRestRepository;

    @Mock
    private S3FileTransferRepository s3FileTransferRepository;

    @Mock
    private FileApiApi fileApi;

    private DocumentStorageFileRestRepository documentStorageFileRestRepository;

    @BeforeEach
    public void beforeEach() {
        this.documentStorageFileRestRepository = new DocumentStorageFileRestRepository(this.presignedUrlRestRepository, this.s3FileTransferRepository,
                this.fileApi);
        reset(this.presignedUrlRestRepository, this.s3FileTransferRepository, this.fileApi);
    }

    @Test
    void testGetFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final int expireInMinutes = 10;

        when(this.presignedUrlRestRepository.getPresignedUrlGetFile(PATH_TO_FILE, expireInMinutes)).thenReturn(PRESIGNED_URL);
        when(this.s3FileTransferRepository.getFile(PRESIGNED_URL)).thenReturn(new byte[] {});
        this.documentStorageFileRestRepository.getFile(PATH_TO_FILE, expireInMinutes);

        verify(this.presignedUrlRestRepository, times(1)).getPresignedUrlGetFile(PATH_TO_FILE, expireInMinutes);
        verify(this.s3FileTransferRepository, times(1)).getFile(PRESIGNED_URL);
    }

    @Test
    void testGetFileSize() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final FileSizeDto fileSizeDto = new FileSizeDto();
        fileSizeDto.setFileSize(123L);
        final String pathToFile = "path/to/file";

        when(fileApi.getFileSize(anyString())).thenReturn(Mono.just(fileSizeDto));
        final Long result = documentStorageFileRestRepository.getFileSize(pathToFile);
        assertEquals(123L, result);
        verify(fileApi, times(1)).getFileSize(pathToFile);

        reset(this.fileApi);
        when(fileApi.getFileSize(anyString())).thenThrow(HttpClientErrorException.class);
        assertThrows(DocumentStorageClientErrorException.class, () -> documentStorageFileRestRepository.getFileSize(pathToFile));
        verify(fileApi, times(1)).getFileSize(pathToFile);

        reset(this.fileApi);
        when(fileApi.getFileSize(anyString())).thenThrow(HttpServerErrorException.class);
        assertThrows(DocumentStorageServerErrorException.class, () -> documentStorageFileRestRepository.getFileSize(pathToFile));
        verify(fileApi, times(1)).getFileSize(pathToFile);

        reset(this.fileApi);
        when(fileApi.getFileSize(anyString())).thenThrow(RestClientException.class);
        assertThrows(DocumentStorageException.class, () -> documentStorageFileRestRepository.getFileSize(pathToFile));
        verify(fileApi, times(1)).getFileSize(pathToFile);
    }

    @Test
    void saveFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final byte[] file = { 1, 2, 3, 4, 5, 6, 7 };
        final int expireInMinutes = 10;

        when(this.presignedUrlRestRepository.getPresignedUrlSaveFile(PATH_TO_FILE, expireInMinutes)).thenReturn(PRESIGNED_URL);
        doNothing().when(this.s3FileTransferRepository).saveFile(PRESIGNED_URL, file);
        this.documentStorageFileRestRepository.saveFile(PATH_TO_FILE, file, expireInMinutes);

        verify(this.presignedUrlRestRepository, times(1)).getPresignedUrlSaveFile(PATH_TO_FILE, expireInMinutes);
        verify(this.s3FileTransferRepository, times(1)).saveFile(PRESIGNED_URL, file);
    }

    @Test
    void updateFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final byte[] file = { 1, 2, 3, 4, 5, 6, 7 };
        final int expireInMinutes = 10;

        when(this.presignedUrlRestRepository.getPresignedUrlUpdateFile(PATH_TO_FILE, expireInMinutes)).thenReturn(PRESIGNED_URL);
        doNothing().when(this.s3FileTransferRepository).updateFile(PRESIGNED_URL, file);
        this.documentStorageFileRestRepository.updateFile(PATH_TO_FILE, file, expireInMinutes);

        verify(this.presignedUrlRestRepository, times(1)).getPresignedUrlUpdateFile(PATH_TO_FILE, expireInMinutes);
        verify(this.s3FileTransferRepository, times(1)).updateFile(PRESIGNED_URL, file);
    }

    @Test
    void deleteFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final int expireInMinutes = 10;

        when(this.presignedUrlRestRepository.getPresignedUrlDeleteFile(PATH_TO_FILE, expireInMinutes)).thenReturn(PRESIGNED_URL);
        doNothing().when(this.s3FileTransferRepository).deleteFile(PRESIGNED_URL);
        this.documentStorageFileRestRepository.deleteFile(PATH_TO_FILE, expireInMinutes);

        verify(this.presignedUrlRestRepository, times(1)).getPresignedUrlDeleteFile(PATH_TO_FILE, expireInMinutes);
        verify(this.s3FileTransferRepository, times(1)).deleteFile(PRESIGNED_URL);
    }

}
