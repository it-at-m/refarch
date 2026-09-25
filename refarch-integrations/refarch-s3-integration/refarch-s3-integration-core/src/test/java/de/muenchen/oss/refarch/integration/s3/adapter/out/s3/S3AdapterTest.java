package de.muenchen.oss.refarch.integration.s3.adapter.out.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.oss.refarch.integration.s3.domain.exception.S3PaginationException;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.oss.refarch.integration.s3.domain.model.ListResult;
import de.muenchen.oss.refarch.integration.s3.domain.model.PresignedUrl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetObjectTaggingResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
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
    public static final String PREFIX = "prefix";
    public static final String CONTINUATION_TOKEN = "token-1";
    public static final String PATH = "path";
    public static final String S3_EXCEPTION_MESSAGE = "boom";
    public static final String ETAG = "etag";
    public static final String SOURCE_PATH = "source";
    public static final String TARGET_PATH = "target";
    public static final String TAG_1_KEY = "tag1";
    public static final String TAG_1_VALUE = "val1";
    public static final String TAG_2_KEY = "tag2";
    public static final String TAG_2_VALUE = "val2";
    public static final String DIR_FILE_1 = "dir/file1.txt";
    public static final String DIR_FILE_3 = "dir/file3.txt";
    public static final String SUBDIR_PREFIX = "subdir/";

    private final S3Mapper s3Mapper = new S3Mapper();
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    private S3OutAdapter adapter;

    @BeforeEach
    void setUp() {
        lenient().when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenAnswer(invocation -> new ListObjectsV2Iterable(s3Client, invocation.getArgument(0)));
        final S3ListHelper listHelper = new S3ListHelper(s3Client, s3Mapper);
        adapter = new S3OutAdapter(s3Mapper, s3Client, s3Presigner, listHelper);
    }

    @Test
    void fileExists_true_whenHeadSucceeds() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(
                HeadObjectResponse.builder()
                        .eTag(ETAG)
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
        final ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        assertEquals(BUCKET, requestCaptor.getValue().bucket());
        assertEquals(PATH, requestCaptor.getValue().key());
        assertEquals(tmp.length(), bodyCaptor.getValue().optionalContentLength().orElseThrow());
    }

    @Test
    void saveFile_withUnknownLength() throws Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final byte[] bytes = "hello-world".getBytes(Charset.defaultCharset());
        final InputStream is = new ByteArrayInputStream(bytes);

        when(s3Client.createMultipartUpload((CreateMultipartUploadRequest) any()))
                .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-id").build());
        when(s3Client.uploadPart((UploadPartRequest) any(), any(RequestBody.class)))
                .thenReturn(UploadPartResponse.builder().eTag(ETAG).build());

        adapter.saveFile(ref, is);

        final ArgumentCaptor<CreateMultipartUploadRequest> createCaptor = ArgumentCaptor.forClass(CreateMultipartUploadRequest.class);
        verify(s3Client).createMultipartUpload(createCaptor.capture());
        assertEquals(BUCKET, createCaptor.getValue().bucket());
        assertEquals(PATH, createCaptor.getValue().key());

        final ArgumentCaptor<UploadPartRequest> partCaptor = ArgumentCaptor.forClass(UploadPartRequest.class);
        final ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).uploadPart(partCaptor.capture(), bodyCaptor.capture());
        assertEquals(BUCKET, partCaptor.getValue().bucket());
        assertEquals(PATH, partCaptor.getValue().key());
        assertEquals(bytes.length, partCaptor.getValue().contentLength());
        assertEquals(bytes.length, bodyCaptor.getValue().optionalContentLength().orElseThrow());

        final ArgumentCaptor<CompleteMultipartUploadRequest> completeCaptor = ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);
        verify(s3Client).completeMultipartUpload(completeCaptor.capture());
        assertEquals(BUCKET, completeCaptor.getValue().bucket());
        assertEquals(PATH, completeCaptor.getValue().key());
    }

    @Test
    void testGetFileMetadata() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final Instant now = Instant.now();
        final HeadObjectResponse hdr = HeadObjectResponse.builder()
                .eTag(ETAG)
                .contentLength(10L)
                .lastModified(now)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(hdr);

        final FileMetadata result = adapter.getFileMetadata(ref);
        assertThat(result.path()).isEqualTo(PATH);
        assertThat(result.contentLength()).isEqualTo(10L);
        assertThat(result.eTag()).isEqualTo(ETAG);
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
    @SuppressWarnings("PMD.CloseResource")
    void testGetFileContent() throws S3Exception, IOException {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final InputStream stream = new ByteArrayInputStream("test".getBytes(Charset.defaultCharset()));
        final ResponseInputStream<GetObjectResponse> response = new ResponseInputStream<>(GetObjectResponse.builder().build(), stream);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(response);
        assertEquals("test", new String(adapter.getFileContent(ref).readAllBytes(), Charset.defaultCharset()));
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
    @SuppressWarnings("deprecation")
    void testGetFilesWithPrefix_mapsResults() throws S3Exception {
        final S3Object obj = S3Object.builder().key("k1").size(1L).eTag("t").lastModified(Instant.now()).build();
        final ListObjectsV2Response response = ListObjectsV2Response.builder().contents(obj).isTruncated(false).build();
        when(s3Client.listObjectsV2((ListObjectsV2Request) any())).thenReturn(response);

        final ListResult result = adapter.getFilesWithPrefix(BUCKET, PREFIX, true, 10, null);
        assertThat(result.files()).hasSize(1);
        assertThat(result.files().getFirst().path()).isEqualTo("k1");
        assertThat(result.commonPrefixes()).isEmpty();
        assertThat(result.truncated()).isFalse();
    }

    @Test
    @SuppressWarnings("deprecation")
    void testGetFilesWithPrefix_nonRecursive_filtersImmediateChildren() throws S3Exception {
        final Instant now = Instant.now();
        final S3Object o1 = S3Object.builder().key(DIR_FILE_1).size(1L).eTag("e1").lastModified(now).build();
        final S3Object o2 = S3Object.builder().key(DIR_FILE_3).size(3L).eTag("e3").lastModified(now).build();
        final CommonPrefix p1 = CommonPrefix.builder().prefix(SUBDIR_PREFIX).build();
        final ListObjectsV2Response response = ListObjectsV2Response.builder().contents(o1, o2).commonPrefixes(p1).isTruncated(true).startAfter(DIR_FILE_3)
                .build();
        when(s3Client.listObjectsV2((ListObjectsV2Request) any())).thenReturn(response);

        final ListResult result = adapter.getFilesWithPrefix(BUCKET, "dir", false, 1000, null);

        assertThat(result.files()).extracting(FileMetadata::path)
                .containsExactlyInAnyOrder(DIR_FILE_1, DIR_FILE_3);
        assertThat(result.commonPrefixes()).containsExactly(SUBDIR_PREFIX);
        assertThat(result.truncated()).isTrue();
    }

    @Test
    @SuppressWarnings("deprecation")
    void testGetFilesWithPrefix_throwsDomainException_onSdkError() {
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());
        assertThrows(S3Exception.class, () -> adapter.getFilesWithPrefix(BUCKET, PREFIX, true, 10, null));
    }

    @Test
    void giveSinglePage_whenGettingAllFilesWithPrefix_thenReturnsPageLazily() {
        final S3Object object = S3Object.builder().key("prefix/file.txt").size(1L).build();
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder().contents(object).isTruncated(false).build());

        final Iterable<ListResult> pages = adapter.getFiles(BUCKET, PREFIX, true, 10, null);
        verify(s3Client, never()).listObjectsV2((ListObjectsV2Request) any());

        final List<ListResult> results = StreamSupport.stream(pages.spliterator(), false).toList();

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().files()).extracting(FileMetadata::path).containsExactly("prefix/file.txt");
        verify(s3Client).listObjectsV2((ListObjectsV2Request) any());
    }

    @Test
    void giveMultiplePages_whenGettingAllFilesWithPrefix_thenUsesContinuationTokenAndKeepsResults() {
        final Instant now = Instant.now();
        final S3Object firstObject = S3Object.builder().key("prefix/file-1.txt").size(1L).lastModified(now).build();
        final S3Object secondObject = S3Object.builder().key("prefix/file-2.txt").size(2L).lastModified(now).build();
        final CommonPrefix firstPrefix = CommonPrefix.builder().prefix("prefix/first/").build();
        final CommonPrefix secondPrefix = CommonPrefix.builder().prefix("prefix/second/").build();
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(firstObject)
                        .commonPrefixes(firstPrefix)
                        .isTruncated(true)
                        .nextContinuationToken(CONTINUATION_TOKEN)
                        .build())
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(secondObject)
                        .commonPrefixes(secondPrefix)
                        .isTruncated(false)
                        .build());

        final List<ListResult> pages = StreamSupport.stream(
                adapter.getFiles(BUCKET, PREFIX, false, 1, "start-after").spliterator(), false)
                .toList();

        assertThat(pages).hasSize(2);
        assertThat(pages.get(0).files()).extracting(FileMetadata::path).containsExactly("prefix/file-1.txt");
        assertThat(pages.get(0).commonPrefixes()).containsExactly("prefix/first/");
        assertThat(pages.get(1).files()).extracting(FileMetadata::path).containsExactly("prefix/file-2.txt");
        assertThat(pages.get(1).commonPrefixes()).containsExactly("prefix/second/");

        final ArgumentCaptor<ListObjectsV2Request> requests = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(s3Client, times(2)).listObjectsV2(requests.capture());
        assertThat(requests.getAllValues().getFirst().prefix()).isEqualTo(PREFIX);
        assertThat(requests.getAllValues().getFirst().delimiter()).isEqualTo("/");
        assertThat(requests.getAllValues().getFirst().maxKeys()).isEqualTo(1);
        assertThat(requests.getAllValues().get(0).startAfter()).isEqualTo("start-after");
        assertThat(requests.getAllValues().get(0).continuationToken()).isNull();
        assertThat(requests.getAllValues().get(1).continuationToken()).isEqualTo(CONTINUATION_TOKEN);
    }

    @Test
    void giveEmptyPage_whenGettingAllFilesWithPrefix_thenReturnsEmptyResult() {
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder().isTruncated(false).build());

        final List<ListResult> pages = StreamSupport.stream(
                adapter.getFiles(BUCKET, "missing", true).spliterator(), false)
                .toList();

        assertThat(pages).hasSize(1);
        assertThat(pages.getFirst().files()).isEmpty();
        assertThat(pages.getFirst().commonPrefixes()).isEmpty();
    }

    @Test
    void giveSdkFailureOnLaterPage_whenGettingAllFilesWithPrefix_thenThrowsPaginationException() {
        final RuntimeException sdkException = software.amazon.awssdk.services.s3.model.S3Exception.builder()
                .message(S3_EXCEPTION_MESSAGE).build();
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder().isTruncated(true).nextContinuationToken(CONTINUATION_TOKEN).build())
                .thenThrow(sdkException);

        assertThatThrownBy(() -> StreamSupport.stream(
                adapter.getFiles(BUCKET, PREFIX, true).spliterator(), false)
                .toList())
                .isInstanceOf(S3PaginationException.class)
                .hasCause(sdkException);
    }

    @Test
    void giveFinal_whenGettingAllFilesWithPrefix_thenStopsIteration() {
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder().isTruncated(false).build());

        final List<ListResult> pages = StreamSupport.stream(
                adapter.getFiles(BUCKET, PREFIX, true).spliterator(), false)
                .toList();

        assertThat(pages).hasSize(1);
        verify(s3Client, times(1)).listObjectsV2((ListObjectsV2Request) any());
    }

    @Test
    void giveMultiplePages_whenGettingFilesAsListResult_thenCombinesFilesAndPrefixes() throws S3Exception {
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(S3Object.builder().key("first").build())
                        .commonPrefixes(CommonPrefix.builder().prefix("first/").build())
                        .isTruncated(true)
                        .nextContinuationToken(CONTINUATION_TOKEN)
                        .build())
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(S3Object.builder().key("second").build())
                        .commonPrefixes(CommonPrefix.builder().prefix("second/").build())
                        .isTruncated(false)
                        .build());

        final ListResult result = adapter.getFilesAsListResult(BUCKET, PREFIX, false);

        assertThat(result.files()).extracting(FileMetadata::path).containsExactly("first", "second");
        assertThat(result.commonPrefixes()).containsExactly("first/", "second/");
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void giveEmptyPages_whenGettingFilesAsListResult_thenReturnsEmptyResult() throws S3Exception {
        when(s3Client.listObjectsV2((ListObjectsV2Request) any()))
                .thenReturn(ListObjectsV2Response.builder().isTruncated(false).build());

        final ListResult result = adapter.getFilesAsListResult(BUCKET, PREFIX, true);

        assertThat(result.files()).isEmpty();
        assertThat(result.commonPrefixes()).isEmpty();
        assertThat(result.truncated()).isFalse();
    }

    @Test
    void giveSdkFailure_whenGettingFilesAsListResult_thenThrowsPaginationException() {
        final RuntimeException sdkException = software.amazon.awssdk.services.s3.model.S3Exception.builder()
                .message(S3_EXCEPTION_MESSAGE).build();
        when(s3Client.listObjectsV2((ListObjectsV2Request) any())).thenThrow(sdkException);

        assertThatThrownBy(() -> adapter.getFilesAsListResult(BUCKET, PREFIX, true))
                .isInstanceOf(S3Exception.class);
    }

    @Test
    void setTags_updatesObjectTags() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        final Map<String, String> tags = Map.of(TAG_1_KEY, TAG_1_VALUE, TAG_2_KEY, TAG_2_VALUE);

        adapter.setTags(ref, tags);

        final ArgumentCaptor<PutObjectTaggingRequest> captor = ArgumentCaptor.forClass(PutObjectTaggingRequest.class);
        verify(s3Client).putObjectTagging(captor.capture());
        assertEquals(BUCKET, captor.getValue().bucket());
        assertEquals(PATH, captor.getValue().key());
        assertThat(captor.getValue().tagging().tagSet())
                .containsExactlyInAnyOrder(
                        Tag.builder().key(TAG_1_KEY).value(TAG_1_VALUE).build(),
                        Tag.builder().key(TAG_2_KEY).value(TAG_2_VALUE).build());
    }

    @Test
    void setTags_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build()).when(s3Client)
                .putObjectTagging(any(PutObjectTaggingRequest.class));

        assertThrows(S3Exception.class, () -> adapter.setTags(ref, Map.of("key", "value")));
    }

    @Test
    void readTags_returnsObjectTags() throws S3Exception {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.getObjectTagging(any(GetObjectTaggingRequest.class))).thenReturn(GetObjectTaggingResponse.builder()
                .tagSet(
                        Tag.builder().key(TAG_1_KEY).value(TAG_1_VALUE).build(),
                        Tag.builder().key(TAG_2_KEY).value(TAG_2_VALUE).build())
                .build());

        final Map<String, String> tags = adapter.getTags(ref);

        assertThat(tags).containsExactlyInAnyOrderEntriesOf(Map.of(TAG_1_KEY, TAG_1_VALUE, TAG_2_KEY, TAG_2_VALUE));
    }

    @Test
    void readTags_throwsDomainException_onSdkError() {
        final FileReference ref = new FileReference(BUCKET, PATH);
        when(s3Client.getObjectTagging(any(GetObjectTaggingRequest.class)))
                .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build());

        assertThrows(S3Exception.class, () -> adapter.getTags(ref));
    }

    @Test
    void copyFile_preservesSourceTags() throws S3Exception {
        final FileReference source = new FileReference(BUCKET, SOURCE_PATH);
        final FileReference target = new FileReference(BUCKET, TARGET_PATH);

        adapter.copyFile(source, target);

        final ArgumentCaptor<CopyObjectRequest> captor = ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().destinationBucket());
        assertEquals(TARGET_PATH, captor.getValue().destinationKey());
        assertEquals(BUCKET, captor.getValue().sourceBucket());
        assertEquals(SOURCE_PATH, captor.getValue().sourceKey());
        assertEquals("COPY", captor.getValue().taggingDirectiveAsString());
    }

    @Test
    void copyFile_clearsTags_whenPreserveTagsIsFalse() throws S3Exception {
        final FileReference source = new FileReference(BUCKET, SOURCE_PATH);
        final FileReference target = new FileReference(BUCKET, TARGET_PATH);

        adapter.copyFile(source, target, false);

        final ArgumentCaptor<CopyObjectRequest> captor = ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(s3Client).copyObject(captor.capture());
        assertEquals(BUCKET, captor.getValue().destinationBucket());
        assertEquals(TARGET_PATH, captor.getValue().destinationKey());
        assertEquals(BUCKET, captor.getValue().sourceBucket());
        assertEquals(SOURCE_PATH, captor.getValue().sourceKey());
        assertEquals("REPLACE", captor.getValue().taggingDirectiveAsString());
        assertThat(captor.getValue().tagging()).isEmpty();
    }

    @Test
    void copyFile_throwsDomainException_onSdkError() {
        final FileReference source = new FileReference(BUCKET, SOURCE_PATH);
        final FileReference target = new FileReference(BUCKET, TARGET_PATH);
        doThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder().message(S3_EXCEPTION_MESSAGE).build()).when(s3Client)
                .copyObject(any(CopyObjectRequest.class));

        assertThrows(S3Exception.class, () -> adapter.copyFile(source, target));
    }
}
