package de.muenchen.refarch.integration.s3.client.repository.transfer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageClientErrorException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageException;
import de.muenchen.refarch.integration.s3.client.exception.DocumentStorageServerErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@WireMockTest
class S3FileTransferRepositoryTest {

    private static final String PRESIGNED_URL = "/expected-presigned-url";
    public static final String LOCALHOST_PREFIX = "http://localhost:";
    public static final String INVALID_URL_PATH = "/invalid-url";

    private final S3FileTransferRepository s3FileTransferRepository = new S3FileTransferRepository();

    @Test
    void testGetFile(final WireMockRuntimeInfo wmRuntimeInfo)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        final byte[] file = { 1, 2, 3, 4, 5, 6, 7 };
        final String presignedUrl = baseUrl + PRESIGNED_URL;

        WireMock.stubFor(WireMock.get(PRESIGNED_URL).willReturn(WireMock.aResponse().withBody(file).withStatus(200)));
        final byte[] result = this.s3FileTransferRepository.getFile(presignedUrl);
        assertThat(result, is(file));

        WireMock.stubFor(WireMock.get(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(400)));
        assertThrows(DocumentStorageClientErrorException.class, () -> this.s3FileTransferRepository.getFile(presignedUrl));

        WireMock.stubFor(WireMock.get(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(500)));
        assertThrows(DocumentStorageServerErrorException.class, () -> this.s3FileTransferRepository.getFile(presignedUrl));

        assertThrows(DocumentStorageException.class,
                () -> this.s3FileTransferRepository.getFile(LOCALHOST_PREFIX + (wmRuntimeInfo.getHttpPort() + 1) + INVALID_URL_PATH));
    }

    @Test
    void saveFile(final WireMockRuntimeInfo wmRuntimeInfo)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        final byte[] file = { 1, 2, 3, 4, 5, 6, 7 };
        final String presignedUrl = baseUrl + PRESIGNED_URL;

        WireMock.stubFor(WireMock.put(PRESIGNED_URL).withRequestBody(WireMock.binaryEqualTo(file)).willReturn(WireMock.ok()));
        this.s3FileTransferRepository.saveFile(presignedUrl, file);

        WireMock.stubFor(WireMock.put(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(400)));
        assertThrows(DocumentStorageClientErrorException.class, () -> this.s3FileTransferRepository.saveFile(presignedUrl, file));

        WireMock.stubFor(WireMock.put(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(500)));
        assertThrows(DocumentStorageServerErrorException.class, () -> this.s3FileTransferRepository.saveFile(presignedUrl, file));

        assertThrows(DocumentStorageException.class,
                () -> this.s3FileTransferRepository.saveFile(LOCALHOST_PREFIX + (wmRuntimeInfo.getHttpPort() + 1) + INVALID_URL_PATH, file));
    }

    @Test
    void updateFile(final WireMockRuntimeInfo wmRuntimeInfo)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        final byte[] file = { 1, 2, 3, 4, 5, 6, 7 };
        final String presignedUrl = baseUrl + PRESIGNED_URL;

        WireMock.stubFor(WireMock.put(PRESIGNED_URL).withRequestBody(WireMock.binaryEqualTo(file)).willReturn(WireMock.ok()));
        this.s3FileTransferRepository.updateFile(presignedUrl, file);

        WireMock.stubFor(WireMock.put(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(400)));
        assertThrows(DocumentStorageClientErrorException.class, () -> this.s3FileTransferRepository.updateFile(presignedUrl, file));

        WireMock.stubFor(WireMock.put(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(500)));
        assertThrows(DocumentStorageServerErrorException.class, () -> this.s3FileTransferRepository.updateFile(presignedUrl, file));

        assertThrows(DocumentStorageException.class,
                () -> this.s3FileTransferRepository.updateFile(LOCALHOST_PREFIX + (wmRuntimeInfo.getHttpPort() + 1) + INVALID_URL_PATH, file));
    }

    @Test
    void deleteFile(final WireMockRuntimeInfo wmRuntimeInfo)
            throws DocumentStorageException, DocumentStorageClientErrorException, DocumentStorageServerErrorException {
        final String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        final String presignedUrl = baseUrl + PRESIGNED_URL;

        WireMock.stubFor(WireMock.delete(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(204)));
        this.s3FileTransferRepository.deleteFile(presignedUrl);

        WireMock.stubFor(WireMock.delete(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(400)));
        assertThrows(DocumentStorageClientErrorException.class, () -> this.s3FileTransferRepository.deleteFile(presignedUrl));

        WireMock.stubFor(WireMock.delete(PRESIGNED_URL).willReturn(WireMock.aResponse().withStatus(500)));
        assertThrows(DocumentStorageServerErrorException.class, () -> this.s3FileTransferRepository.deleteFile(presignedUrl));

        assertThrows(DocumentStorageException.class,
                () -> this.s3FileTransferRepository.deleteFile(LOCALHOST_PREFIX + (wmRuntimeInfo.getHttpPort() + 1) + INVALID_URL_PATH));

    }

}
