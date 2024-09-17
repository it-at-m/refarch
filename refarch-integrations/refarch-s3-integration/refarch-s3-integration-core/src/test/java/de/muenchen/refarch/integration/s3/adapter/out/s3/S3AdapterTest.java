package de.muenchen.refarch.integration.s3.adapter.out.s3;

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
import org.junit.jupiter.api.Assertions;
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
public class S3AdapterTest {

    private final String s3Url = "http://localhost:9000";
    private final String filePath = "test/image.png";
    private final List<Method> actions = List.of(Method.GET, Method.POST, Method.PUT, Method.DELETE);
    private final int expiresInMinutes = 5;
    private S3Adapter s3Adapter;
    @Mock
    private MinioClient client;

    @BeforeEach
    public void beforeEach() throws FileSystemAccessException {
        this.s3Adapter = new S3Adapter("test-bucket", this.client, false);
    }

    @Test
    public void testGetPresignedUrl()
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException, FileSystemAccessException {
        Mockito.when(this.client.getPresignedObjectUrl(Mockito.any(GetPresignedObjectUrlArgs.class))).thenReturn(this.s3Url + "/some-url/...");
        for (final Method action : this.actions) {
            final String presignedUrl = this.s3Adapter.getPresignedUrl(this.filePath, action, this.expiresInMinutes);
            Assertions.assertTrue(presignedUrl.contains(this.s3Url));
        }
    }
}
