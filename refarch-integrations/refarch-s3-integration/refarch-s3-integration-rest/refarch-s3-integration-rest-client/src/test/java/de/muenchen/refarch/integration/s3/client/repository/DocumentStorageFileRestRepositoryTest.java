package de.muenchen.refarch.integration.s3.client.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import org.mockito.Mockito;
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
        Mockito.reset(this.presignedUrlRestRepository, this.s3FileTransferRepository, this.fileApi);
    }

    @Test
    void getFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;
        final String presignedUrl = "the_presignedUrl";

        when(this.presignedUrlRestRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes)).thenReturn(presignedUrl);
        when(this.s3FileTransferRepository.getFile(presignedUrl)).thenReturn(new byte[] {});
        this.documentStorageFileRestRepository.getFile(pathToFile, expireInMinutes);

        verify(this.presignedUrlRestRepository, Mockito.times(1)).getPresignedUrlGetFile(pathToFile, expireInMinutes);
        verify(this.s3FileTransferRepository, Mockito.times(1)).getFile(presignedUrl);
    }

    @Test
    void getFileSize() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final FileSizeDto fileSizeDto = new FileSizeDto();
        fileSizeDto.setFileSize(123L);
        final String pathToFile = "path/to/file";

        when(fileApi.getFileSize(anyString())).thenReturn(Mono.just(fileSizeDto));
        final Long result = documentStorageFileRestRepository.getFileSize(pathToFile);
        assertEquals(123L, result);
        verify(fileApi, Mockito.times(1)).getFileSize(pathToFile);

        Mockito.reset(this.fileApi);
        when(fileApi.getFileSize(anyString())).thenThrow(HttpClientErrorException.class);
        assertThrows(DocumentStorageClientErrorException.class, () -> documentStorageFileRestRepository.getFileSize(pathToFile));
        verify(fileApi, Mockito.times(1)).getFileSize(pathToFile);

        Mockito.reset(this.fileApi);
        when(fileApi.getFileSize(anyString())).thenThrow(HttpServerErrorException.class);
        assertThrows(DocumentStorageServerErrorException.class, () -> documentStorageFileRestRepository.getFileSize(pathToFile));
        verify(fileApi, Mockito.times(1)).getFileSize(pathToFile);

        Mockito.reset(this.fileApi);
        when(fileApi.getFileSize(anyString())).thenThrow(RestClientException.class);
        assertThrows(DocumentStorageException.class, () -> documentStorageFileRestRepository.getFileSize(pathToFile));
        verify(fileApi, Mockito.times(1)).getFileSize(pathToFile);
    }

    @Test
    void saveFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final byte[] file = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        final int expireInMinutes = 10;
        final String presignedUrl = "the_presignedUrl";

        when(this.presignedUrlRestRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes)).thenReturn(presignedUrl);
        doNothing().when(this.s3FileTransferRepository).saveFile(presignedUrl, file);
        this.documentStorageFileRestRepository.saveFile(pathToFile, file, expireInMinutes);

        verify(this.presignedUrlRestRepository, Mockito.times(1)).getPresignedUrlSaveFile(pathToFile, expireInMinutes);
        verify(this.s3FileTransferRepository, Mockito.times(1)).saveFile(presignedUrl, file);
    }

    @Test
    void updateFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final byte[] file = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        final int expireInMinutes = 10;
        final String presignedUrl = "the_presignedUrl";

        when(this.presignedUrlRestRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes)).thenReturn(presignedUrl);
        doNothing().when(this.s3FileTransferRepository).updateFile(presignedUrl, file);
        this.documentStorageFileRestRepository.updateFile(pathToFile, file, expireInMinutes);

        verify(this.presignedUrlRestRepository, Mockito.times(1)).getPresignedUrlUpdateFile(pathToFile, expireInMinutes);
        verify(this.s3FileTransferRepository, Mockito.times(1)).updateFile(presignedUrl, file);
    }

    @Test
    void deleteFile() throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;
        final String presignedUrl = "the_presignedUrl";

        when(this.presignedUrlRestRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes)).thenReturn(presignedUrl);
        doNothing().when(this.s3FileTransferRepository).deleteFile(presignedUrl);
        this.documentStorageFileRestRepository.deleteFile(pathToFile, expireInMinutes);

        verify(this.presignedUrlRestRepository, Mockito.times(1)).getPresignedUrlDeleteFile(pathToFile, expireInMinutes);
        verify(this.s3FileTransferRepository, Mockito.times(1)).deleteFile(presignedUrl);
    }

}
