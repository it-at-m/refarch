package de.muenchen.refarch.s3.integration.adapter.out.s3;

import de.muenchen.refarch.s3.integration.domain.exception.FileSystemAccessException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
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
 * Tests for {@link S3Adapter} creating presigned urls correctly and using a proxy if enabled
 *
 * @author ext.dl.moesle
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class S3AdapterTest {

    private final String s3Url = "http://localhost:9000";
    private final String s3ProxyUrl = "http://127.0.0.1:9000";
    private final String filePath = "test/image.png";
    private final List<Method> actions = List.of(Method.GET, Method.POST, Method.PUT, Method.DELETE);
    private final int expiresInMinutes = 5;
    private S3Adapter s3Adapter;
    @Mock
    private MinioClient client;

    @BeforeEach
    public void beforeEach() throws FileSystemAccessException {
        this.s3Adapter = new S3Adapter("test-bucket", this.s3Url, this.client, false, Optional.empty());
    }

    @Test
    public void testGetPresignedUrlWithoutProxy()
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException, FileSystemAccessException {
        Mockito.when(this.client.getPresignedObjectUrl(Mockito.any(GetPresignedObjectUrlArgs.class))).thenReturn(this.s3Url + "/some-url/...");
        for (final Method action : this.actions) {
            final String presignedUrl = this.s3Adapter.getPresignedUrl(this.filePath, action, this.expiresInMinutes);
            Assertions.assertTrue(presignedUrl.contains(this.s3Url));
            Assertions.assertFalse(presignedUrl.contains(this.s3ProxyUrl));
        }
    }

    @Test
    public void testGetPresignedUrlWithProxy()
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException, FileSystemAccessException {
        this.s3Adapter = new S3Adapter("test-bucket", this.s3Url, this.client, false, Optional.of(this.s3ProxyUrl));
        Mockito.when(this.client.getPresignedObjectUrl(Mockito.any(GetPresignedObjectUrlArgs.class))).thenReturn(this.s3Url + "/some-url/...");
        for (final Method action : this.actions) {
            final String presignedUrl = this.s3Adapter.getPresignedUrl(this.filePath, action, this.expiresInMinutes);
            Assertions.assertTrue(presignedUrl.contains(this.s3ProxyUrl));
            Assertions.assertFalse(presignedUrl.contains(this.s3Url));
        }
    }

}
