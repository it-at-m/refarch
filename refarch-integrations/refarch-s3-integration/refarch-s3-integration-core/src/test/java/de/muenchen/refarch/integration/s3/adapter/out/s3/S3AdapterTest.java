package de.muenchen.refarch.integration.s3.adapter.out.s3;

import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3AdapterTest {

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
    void fileExists_returnsTrue_whenHeadSucceeds() throws S3Exception {
        final FileReference ref = new FileReference("bucket", "path");
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(
                HeadObjectResponse.builder()
                        .eTag("etag")
                        .contentLength(1L)
                        .lastModified(Instant.now())
                        .build());

        boolean exists = adapter.fileExists(ref);
        assertThat(exists).isTrue();
    }

    @Test
    void fileExists_returnsFalse_whenNoSuchKey() throws S3Exception {
        final FileReference ref = new FileReference("bucket", "path");
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("not found").build());

        boolean exists = adapter.fileExists(ref);
        assertThat(exists).isFalse();
    }

    @Test
    void fileExists_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference("bucket", "path");
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("boom").build());

        assertThrows(S3Exception.class, () -> adapter.fileExists(ref));
    }

    @Test
    void saveFile_inputStream_putsObject() throws S3Exception {
        final FileReference ref = new FileReference("bucket", "path");
        final byte[] content = "hello".getBytes();

        adapter.saveFile(ref, new ByteArrayInputStream(content), content.length);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        assertEquals("bucket", requestCaptor.getValue().bucket());
        assertEquals("path", requestCaptor.getValue().key());
    }

    @Test
    void saveFile_inputStream_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference("bucket", "path");
        final byte[] content = "hello".getBytes();
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("fail").build())
                .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(S3Exception.class, () -> adapter.saveFile(ref, new ByteArrayInputStream(content), content.length));
    }

    @Test
    void saveFile_file_putsObject() throws Exception {
        final FileReference ref = new FileReference("bucket", "path");
        final File tmp = File.createTempFile("s3-test", ".bin");
        tmp.deleteOnExit();

        adapter.saveFile(ref, tmp);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertEquals("bucket", requestCaptor.getValue().bucket());
        assertEquals("path", requestCaptor.getValue().key());
    }

    @Test
    void getFileMetadata_returnsMappedValue() throws S3Exception {
        final FileReference ref = new FileReference("bucket", "path");
        final Instant now = Instant.now();
        HeadObjectResponse hdr = HeadObjectResponse.builder()
                .eTag("etag")
                .contentLength(10L)
                .lastModified(now)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(hdr);

        FileMetadata result = adapter.getFileMetadata(ref);
        assertThat(result.path()).isEqualTo("path");
        assertThat(result.contentLength()).isEqualTo(10L);
        assertThat(result.eTag()).isEqualTo("etag");
        assertThat(result.lastModified()).isEqualTo(now);
    }

    @Test
    void getFileMetadata_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference("bucket", "path");
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("boom").build());

        assertThrows(S3Exception.class, () -> adapter.getFileMetadata(ref));
    }

    @Test
    void getPresignedUrl_forGetPutDeleteHead() throws S3Exception, MalformedURLException {
        final FileReference ref = new FileReference("bucket", "key");
        final URI uri = URI.create("https://example.com/url");

        PresignedGetObjectRequest mockGet = mock(PresignedGetObjectRequest.class);
        when(mockGet.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignGetObject((GetObjectPresignRequest) any())).thenReturn(mockGet);

        PresignedPutObjectRequest mockPut = mock(PresignedPutObjectRequest.class);
        when(mockPut.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any())).thenReturn(mockPut);

        PresignedDeleteObjectRequest mockDelete = mock(PresignedDeleteObjectRequest.class);
        when(mockDelete.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignDeleteObject((DeleteObjectPresignRequest) any())).thenReturn(mockDelete);

        PresignedHeadObjectRequest mockHead = mock(PresignedHeadObjectRequest.class);
        when(mockHead.url()).thenReturn(uri.toURL());
        when(s3Presigner.presignHeadObject((HeadObjectPresignRequest) any())).thenReturn(mockHead);

        PresignedUrl getUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.GET, java.time.Duration.ofMinutes(1));
        PresignedUrl putUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.PUT, java.time.Duration.ofMinutes(1));
        PresignedUrl delUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.DELETE, java.time.Duration.ofMinutes(1));
        PresignedUrl headUrl = adapter.getPresignedUrl(ref, PresignedUrl.Action.HEAD, java.time.Duration.ofMinutes(1));

        assertThat(getUrl.url()).isEqualTo(uri.toURL());
        assertThat(putUrl.url()).isEqualTo(uri.toURL());
        assertThat(delUrl.url()).isEqualTo(uri.toURL());
        assertThat(headUrl.url()).isEqualTo(uri.toURL());
    }

    @Test
    void getPresignedUrl_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference("bucket", "key");
        when(s3Presigner.presignGetObject((GetObjectPresignRequest) any())).thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("boom").build());
        assertThrows(S3Exception.class, () -> adapter.getPresignedUrl(ref, PresignedUrl.Action.GET, java.time.Duration.ofMinutes(1)));
    }

    @Test
    void getFileContent_returnsStream() throws S3Exception {
        final FileReference ref = new FileReference("bucket", "key");
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(null);
        assertThat(adapter.getFileContent(ref)).isNull();
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    void getFileContent_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference("bucket", "key");
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("boom").build());
        assertThrows(S3Exception.class, () -> adapter.getFileContent(ref));
    }

    @Test
    void deleteFile_invokesClient() throws S3Exception {
        final FileReference ref = new FileReference("bucket", "key");
        adapter.deleteFile(ref);
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("bucket", captor.getValue().bucket());
        assertEquals("key", captor.getValue().key());
    }

    @Test
    void deleteFile_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference("bucket", "key");
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("boom").build()).when(s3Client).deleteObject(any(DeleteObjectRequest.class));
        assertThrows(S3Exception.class, () -> adapter.deleteFile(ref));
    }

    @Test
    void getFilesWithPrefix_mapsResults() throws S3Exception {
        final S3Object obj = S3Object.builder().key("k1").size(1L).eTag("t").lastModified(Instant.now()).build();
        final ListObjectsResponse response = ListObjectsResponse.builder().contents(obj).build();
        when(s3Client.listObjects((ListObjectsRequest) any())).thenReturn(response);

        List<FileMetadata> list = adapter.getFilesWithPrefix("bucket", "prefix", 10, null);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().path()).isEqualTo("k1");
    }

    @Test
    void getFilesWithPrefix_throwsDomainException_onSdkError() {
        when(s3Client.listObjects((ListObjectsRequest) any())).thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message("boom").build());
        assertThrows(S3Exception.class, () -> adapter.getFilesWithPrefix("bucket", "prefix", 10, null));
    }
}
