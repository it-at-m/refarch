package de.muenchen.refarch.integration.s3.adapter.out.s3;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.muenchen.refarch.integration.s3.domain.exception.FileSystemAccessException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests for {@link S3Adapter} creating presigned urls correctly
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3AdapterTest {

    private static final String S3_URL = "http://localhost:9000";
    private static final String FILE_PATH = "test/image.png";
    private static final List<Method> ACTIONS = List.of(Method.GET, Method.POST, Method.PUT, Method.DELETE);
    private static final int EXPIRES_IN_MINUTES = 5;
    private S3Adapter s3Adapter;
    @Mock
    private MinioClient client;

    @BeforeEach
    void beforeEach() throws FileSystemAccessException {
        this.s3Adapter = new S3Adapter("test-bucket", this.client);
    }

    @Test
    void testGetPresignedUrl()
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException, FileSystemAccessException {
        Mockito.when(this.client.getPresignedObjectUrl(Mockito.any(GetPresignedObjectUrlArgs.class))).thenReturn(this.S3_URL + "/some-url/...");
        for (final Method action : this.ACTIONS) {
            final String presignedUrl = this.s3Adapter.getPresignedUrl(this.FILE_PATH, action, this.EXPIRES_IN_MINUTES);
            assertTrue(presignedUrl.contains(this.S3_URL));
        }
    }
}
