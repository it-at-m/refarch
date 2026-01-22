package de.muenchen.refarch.integration.s3.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedHeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.CouplingBetweenObjects")
class S3AdapterTest {

    public static final String BUCKET = "bucket";
    public static final String PATH = "path";
    public static final String S3_EXCEPTION_MESSAGE = "boom";
    private final S3Mapper s3Mapper = new S3Mapper();
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    private S3Adapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new S3Adapter(s3Mapper, s3Client, s3Presigner);
    }

    @Test
    void fileExists_true_whenHeadSucceeds() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(
                HeadObjectResponse.builder()
                        .eTag("etag")
                        .contentLength(1L)
                        .lastModified(Instant.now())
                        .build());

        final boolean exists = adapter.fileExists(ref);
        assertThat(exists).isTrue();
    }

    @Test
    void fileExists_false_whenNoSuchKey() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("not found").build());

        final boolean exists = adapter.fileExists(ref);
        assertThat(exists).isFalse();
    }

    @Test
    void fileExists_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());

        assertThrows(S3Exception.class, () -> adapter.fileExists(ref));
    }

    @Test
    void saveFile_inputStream_putsObject() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final byte[] content = "hello".getBytes(Charset.defaultCharset());

        adapter.saveFile(ref, new ByteArrayInputStream(content), content.length);

        final ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        final ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        assertEquals(BUCKET, requestCaptor.getValue().bucket());
        assertEquals(PATH, requestCaptor.getValue().key());
    }

    @Test
    void saveFile_inputStream_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final byte[] content = "hello".getBytes(Charset.defaultCharset());
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("fail").build())
                .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(S3Exception.class, () -> adapter.saveFile(ref, new ByteArrayInputStream(content), content.length));
    }

    @Test
    void saveFile_file_putsObject() throws Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final File tmp = Files.createTempFile("s3-test", ".bin").toFile();
        tmp.deleteOnExit();

        adapter.saveFile(ref, tmp);

        final ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertEquals(BUCKET, requestCaptor.getValue().bucket());
        assertEquals(PATH, requestCaptor.getValue().key());
    }

    @Test
    void testGetFileMetadata() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final Instant now = Instant.now();
        final HeadObjectResponse hdr = HeadObjectResponse.builder()
                .eTag("etag")
                .contentLength(10L)
                .lastModified(now)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(hdr);

        final FileMetadata result = adapter.getFileMetadata(ref);
        assertThat(result.path()).isEqualTo(PATH);
        assertThat(result.contentLength()).isEqualTo(10L);
        assertThat(result.eTag()).isEqualTo("etag");
        assertThat(result.lastModified()).isEqualTo(now);
    }

    @Test
    void testGetFileMetadata_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());

        assertThrows(S3Exception.class, () -> adapter.getFileMetadata(ref));
    }

    @Test
    void testGetPresignedUrl_forGetPutDeleteHead() throws S3Exception, MalformedURLException {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final URI uri = URI.create("https://example.com/url");

        final PresignedGetObjectRequest mockGet = mock(PresignedGetObjectRequest.class);
        when(mockGet.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignGetObject((GetObjectPresignRequest) any())).thenReturn(mockGet);

        final PresignedPutObjectRequest mockPut = mock(PresignedPutObjectRequest.class);
        when(mockPut.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any())).thenReturn(mockPut);

        final PresignedDeleteObjectRequest mockDelete = mock(PresignedDeleteObjectRequest.class);
        when(mockDelete.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignDeleteObject((DeleteObjectPresignRequest) any())).thenReturn(mockDelete);

        final PresignedHeadObjectRequest mockHead = mock(PresignedHeadObjectRequest.class);
        when(mockHead.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignHeadObject((HeadObjectPresignRequest) any())).thenReturn(mockHead);

        final PresignedUrl getUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.GET, java.time.Duration.ofMinutes(1));
        final PresignedUrl putUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.PUT, java.time.Duration.ofMinutes(1));
        final PresignedUrl delUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.DELETE, java.time.Duration.ofMinutes(1));
        final PresignedUrl headUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.HEAD, java.time.Duration.ofMinutes(1));

        assertThat(getUrl.url()).isEqualTo(uri.toURL());
        assertThat(putUrl.url()).isEqualTo(uri.toURL());
        assertThat(delUrl.url()).isEqualTo(uri.toURL());
        assertThat(headUrl.url()).isEqualTo(uri.toURL());
    }

    @Test
    void testGetPresignedUrl_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Presigner.presignGetObject((GetObjectPresignRequest) any()))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());
        assertThrows(S3Exception.class, () -> adapter.getPresignedUrl(ref, PresignedUrl.Action.GET, java.time.Duration.ofMinutes(1)));
    }

    @Test
    void testGetFileContent() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(null);
        assertThat(adapter.getFileContent(ref)).isNull();
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    void testGetFileContent_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());
        assertThrows(S3Exception.class, () -> adapter.getFileContent(ref));
    }

    @Test
    void deleteFile_invokesClient() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        adapter.deleteFile(ref);
        final ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals(PATH, captor.getValue().key());
    }

    @Test
    void deleteFile_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build()).when(s3Client)
                .deleteObject(any(DeleteObjectRequest.class));
        assertThrows(S3Exception.class, () -> adapter.deleteFile(ref));
    }

    @Test
    void testGetFilesWithPrefix_mapsResults() throws S3Exception {
        final S3Object obj = S3Object.builder().key("k1").size(1L).eTag("t").lastModified(Instant.now()).build();
        final ListObjectsResponse response = ListObjectsResponse.builder().contents(obj).build();
        when(s3Client.listObjects((ListObjectsRequest) any())).thenReturn(response);

        final List<FileMetadata> list = adapter.getFilesWithPrefix(BUCKET, "prefix", 10, null);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().path()).isEqualTo("k1");
    }

    @Test
    void testGetFilesWithPrefix_throwsDomainException_onSdkError() {
        when(s3Client.listObjects((ListObjectsRequest) any()))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());
        assertThrows(S3Exception.class, () -> adapter.getFilesWithPrefix(BUCKET, "prefix", 10, null));
    }
}
