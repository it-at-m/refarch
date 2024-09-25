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

    public static final String PATH_TO_FILE = "folder/file.txt";
    public static final String PRESIGNED_URL = "the_presignedUrl";
    public static final String SOMETHING_HAPPENED = "Something happened";
    @Mock
    private FileApiApi fileApi;

    private PresignedUrlRestRepository presignedUrlRestRepository;

    @BeforeEach
    public void beforeEach() {
        this.presignedUrlRestRepository = new PresignedUrlRestRepository(this.fileApi);
        Mockito.reset(this.fileApi);
    }

    @Test
    void testGetPresignedUrlGetFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final int expireInMinutes = 10;

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl(PRESIGNED_URL);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(PATH_TO_FILE, expireInMinutes)).thenReturn(Mono.just(expected));
        final String result = this.presignedUrlRestRepository.getPresignedUrlGetFile(PATH_TO_FILE, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).get(PATH_TO_FILE, expireInMinutes);
        assertThat(result, is(expected.getUrl()));
        Mockito.reset(this.fileApi);
    }

    @Test
    void testGetPresignedUrlGetFileException() {
        final int expireInMinutes = 10;

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(PATH_TO_FILE, expireInMinutes)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlGetFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).get(PATH_TO_FILE, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(PATH_TO_FILE, expireInMinutes)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlGetFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).get(PATH_TO_FILE, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.get(PATH_TO_FILE, expireInMinutes)).thenThrow(new RestClientException(SOMETHING_HAPPENED));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlGetFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).get(PATH_TO_FILE, expireInMinutes);
    }

    @Test
    void testGetPresignedUrlSaveFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(PATH_TO_FILE);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl(PRESIGNED_URL);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenReturn(Mono.just(expected));

        final String result = this.presignedUrlRestRepository.getPresignedUrlSaveFile(PATH_TO_FILE, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);
        assertThat(result, is(expected.getUrl()));
    }

    @Test
    void testGetPresignedUrlSaveFileException() {
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(PATH_TO_FILE);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlSaveFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlSaveFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.save(fileDataDto)).thenThrow(new RestClientException(SOMETHING_HAPPENED));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlSaveFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).save(fileDataDto);
    }

    @Test
    void testGetPresignedUrlUpdateFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(PATH_TO_FILE);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl(PRESIGNED_URL);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenReturn(Mono.just(expected));

        final String result = this.presignedUrlRestRepository.getPresignedUrlUpdateFile(PATH_TO_FILE, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);
        assertThat(result, is(expected.getUrl()));
    }

    @Test
    void testGetPresignedUrlUpdateFileException() {
        final int expireInMinutes = 10;

        final FileDataDto fileDataDto = new FileDataDto();
        fileDataDto.setPathToFile(PATH_TO_FILE);
        fileDataDto.setExpiresInMinutes(expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlUpdateFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlUpdateFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.update(fileDataDto)).thenThrow(new RestClientException(SOMETHING_HAPPENED));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlUpdateFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).update(fileDataDto);
    }

    @Test
    void testGetPresignedUrlDeleteFile()
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final int expireInMinutes = 10;

        final PresignedUrlDto expected = new PresignedUrlDto();
        expected.setUrl(PRESIGNED_URL);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(PATH_TO_FILE, expireInMinutes)).thenReturn(Mono.just(expected));

        final String result = this.presignedUrlRestRepository.getPresignedUrlDeleteFile(PATH_TO_FILE, expireInMinutes);
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(PATH_TO_FILE, expireInMinutes);
        assertThat(result, is(expected.getUrl()));
    }

    @Test
    void testGetPresignedUrlDeleteFileException() {
        final int expireInMinutes = 10;

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(PATH_TO_FILE, expireInMinutes)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(DocumentStorageClientErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlDeleteFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(PATH_TO_FILE, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(PATH_TO_FILE, expireInMinutes)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        Assertions.assertThrows(DocumentStorageServerErrorException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlDeleteFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(PATH_TO_FILE, expireInMinutes);

        Mockito.reset(this.fileApi);
        Mockito.when(this.fileApi.delete1(PATH_TO_FILE, expireInMinutes)).thenThrow(new RestClientException(SOMETHING_HAPPENED));
        Assertions.assertThrows(DocumentStorageException.class,
                () -> this.presignedUrlRestRepository.getPresignedUrlDeleteFile(PATH_TO_FILE, expireInMinutes));
        Mockito.verify(this.fileApi, Mockito.times(1)).delete1(PATH_TO_FILE, expireInMinutes);
    }

}
