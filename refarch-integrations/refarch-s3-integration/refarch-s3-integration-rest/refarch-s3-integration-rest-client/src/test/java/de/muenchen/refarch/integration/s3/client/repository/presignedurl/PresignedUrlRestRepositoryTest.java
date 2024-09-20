package de.muenchen.refarch.integration.s3.client.repository.presignedurl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.muenchen.refarch.integration.s3.client.api.FileApiApi;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import de.muenchen.refarch.integration.s3.client.model.FileDataDto;
import de.muenchen.refarch.integration.s3.client.model.PresignedUrlDto;
import org.junit.jupiter.api.Assertions;
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
class PresignedUrlRestRepositoryTest {

    @Mock
    private FileApiApi fileApi;

    private PresignedUrlRestRepository presignedUrlRestRepository;

    @BeforeEach
    public void beforeEach() {
        this.presignedUrlRestRepository = new PresignedUrlRestRepository(this.fileApi);
        Mockito.reset(this.fileApi);
    }

    @Test
    void getPresignedUrlGetFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl("the_presignedUrl");

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(pathToFile, expireInMinutes)).thenReturn(Mono.just(expected));
        final String result = this.presignedUrlRestRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).get(pathToFile, expireInMinutes);
        assertThat(result, is(expected.getUrl()));
        Mockito.reset(this.fileApi);
    }

    @Test
    void getPresignedUrlGetFileException() {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(pathToFile, expireInMinutes)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).get(pathToFile, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(pathToFile, expireInMinutes)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).get(pathToFile, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(pathToFile, expireInMinutes)).thenThrow(new RestClientException("Something happened"));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlGetFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).get(pathToFile, expireInMinutes);
    }

    @Test
    void getPresignedUrlSaveFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(pathToFile);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl("the_presignedUrl");

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenReturn(Mono.just(expected));

        final String result = this.presignedUrlRestRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);
        assertThat(result, is(expected.getUrl()));
    }

    @Test
    void getPresignedUrlSaveFileException() {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(pathToFile);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenThrow(new RestClientException("Something happened"));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlSaveFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);
    }

    @Test
    void getPresignedUrlUpdateFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(pathToFile);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl("the_presignedUrl");

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenReturn(Mono.just(expected));

        final String result = this.presignedUrlRestRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);
        assertThat(result, is(expected.getUrl()));
    }

    @Test
    void getPresignedUrlUpdateFileException() {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(pathToFile);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenThrow(new RestClientException("Something happened"));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlUpdateFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);
    }

    @Test
    void getPresignedUrlDeleteFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl("the_presignedUrl");

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(pathToFile, expireInMinutes)).thenReturn(Mono.just(expected));

        final String result = this.presignedUrlRestRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(pathToFile, expireInMinutes);
        assertThat(result, is(expected.getUrl()));
    }

    @Test
    void getPresignedUrlDeleteFileException() {
        final String pathToFile = "folder/file.txt";
        final int expireInMinutes = 10;

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(pathToFile, expireInMinutes)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(pathToFile, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(pathToFile, expireInMinutes)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(pathToFile, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(pathToFile, expireInMinutes)).thenThrow(new RestClientException("Something happened"));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlDeleteFile(pathToFile, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(pathToFile, expireInMinutes);
    }

}
