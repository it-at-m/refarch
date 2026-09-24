package de.muenchen.oss.refarch.integration.s3;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.refarch.integration.s3.adapter.out.s3.S3ListHelper;
import de.muenchen.oss.refarch.integration.s3.adapter.out.s3.S3Mapper;
import de.muenchen.oss.refarch.integration.s3.adapter.out.s3.S3OutAdapter;
import de.muenchen.oss.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.oss.refarch.integration.s3.domain.model.ListResult;
import de.muenchen.oss.refarch.integration.s3.domain.model.PresignedUrl;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class E2ETest {

    private static final String ACCESS_KEY = "minio";
    private static final String SECRET_KEY = "Test1234";
    private static final String BUCKET = "test-bucket";

    @Container
    private static final GenericContainer<?> MINIO = new GenericContainer<>("quay.io/minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server", "/data", "--console-address", ":9001")
            .withExposedPorts(9000, 9001);

    private S3OutPort s3OutPort;

    @BeforeAll
    @SuppressWarnings("PMD.CloseResource")
    void setUp() {
        final String endpoint = "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(9000);
        final Region region = Region.US_EAST_1;

        final S3Configuration s3cfg = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        final StaticCredentialsProvider creds = StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY));

        final S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(region)
                .credentialsProvider(creds)
                .serviceConfiguration(s3cfg)
                .build();
        final S3Presigner s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(region)
                .credentialsProvider(creds)
                .serviceConfiguration(s3cfg)
                .build();

        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {
        }

        final S3Mapper mapper = new S3Mapper();
        final S3ListHelper s3ListHelper = new S3ListHelper(s3Client, mapper);
        this.s3OutPort = new S3OutAdapter(mapper, s3Client, s3Presigner, s3ListHelper);
    }

    @Test
    @SuppressWarnings("deprecation")
    void listFilesWithPrefix_handlesPagination() throws Exception {
        final String rootPrefix = "pagination/" + UUID.randomUUID() + "/";
        final String prefix = rootPrefix + "objects/";
        final List<String> keys = List.of(prefix + "a.txt", prefix + "b.txt", prefix + "c.txt", prefix + "d.txt");
        final List<String> nonRecursiveKeys = List.of(rootPrefix + "folders/root.txt", rootPrefix + "folders/nested/a.txt",
                rootPrefix + "folders/nested/b.txt");

        try {
            for (final String key : keys) {
                s3OutPort.saveFile(new FileReference(BUCKET, key), new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8)), key.length());
            }
            for (final String key : nonRecursiveKeys) {
                s3OutPort.saveFile(new FileReference(BUCKET, key), new ByteArrayInputStream(key.getBytes(StandardCharsets.UTF_8)), key.length());
            }

            final ListResult firstPage = s3OutPort.getFilesWithPrefix(BUCKET, prefix, true, 2, null);
            assertThat(firstPage.files()).extracting(FileMetadata::path).containsExactly(keys.get(0), keys.get(1));
            assertThat(firstPage.truncated()).isTrue();

            final ListResult lastPage = s3OutPort.getFilesWithPrefix(BUCKET, prefix, true, 2, keys.get(1));
            assertThat(lastPage.files()).extracting(FileMetadata::path).containsExactly(keys.get(2), keys.get(3));
            assertThat(lastPage.truncated()).isFalse();

            final ListResult exactPage = s3OutPort.getFilesWithPrefix(BUCKET, prefix, true, keys.size(), null);
            assertThat(exactPage.files()).extracting(FileMetadata::path).containsExactlyElementsOf(keys);
            assertThat(exactPage.truncated()).isFalse();

            final List<ListResult> allPages = StreamSupport.stream(
                    s3OutPort.getFiles(BUCKET, prefix, true, 2, null).spliterator(), false)
                    .toList();
            assertThat(allPages).hasSize(2);
            assertThat(allPages.stream()
                    .flatMap(page -> page.files().stream())
                    .map(FileMetadata::path))
                    .containsExactlyElementsOf(keys);

            final ListResult afterLastPage = s3OutPort.getFilesWithPrefix(BUCKET, prefix, true, 2, keys.get(3));
            assertThat(afterLastPage.files()).isEmpty();
            assertThat(afterLastPage.commonPrefixes()).isEmpty();
            assertThat(afterLastPage.truncated()).isFalse();

            final String foldersPrefix = rootPrefix + "folders/";
            final ListResult nonRecursiveFirstPage = s3OutPort.getFilesWithPrefix(BUCKET, foldersPrefix, false, 1, null);
            assertThat(nonRecursiveFirstPage.files()).isEmpty();
            assertThat(nonRecursiveFirstPage.commonPrefixes()).containsExactly(rootPrefix + "folders/nested/");
            assertThat(nonRecursiveFirstPage.truncated()).isTrue();

            final ListResult nonRecursiveLastPage = s3OutPort.getFilesWithPrefix(BUCKET, foldersPrefix, false, 1, rootPrefix + "folders/nested/");
            assertThat(nonRecursiveLastPage.files()).extracting(FileMetadata::path).containsExactly(nonRecursiveKeys.getFirst());
            assertThat(nonRecursiveLastPage.commonPrefixes()).isEmpty();
            assertThat(nonRecursiveLastPage.truncated()).isFalse();

            final ListResult missingPrefix = s3OutPort.getFilesWithPrefix(BUCKET, rootPrefix + "missing/", true, 1, null);
            assertThat(missingPrefix.files()).isEmpty();
            assertThat(missingPrefix.commonPrefixes()).isEmpty();
            assertThat(missingPrefix.truncated()).isFalse();
        } finally {
            for (final String key : keys) {
                s3OutPort.deleteFile(new FileReference(BUCKET, key));
            }
            for (final String key : nonRecursiveKeys) {
                s3OutPort.deleteFile(new FileReference(BUCKET, key));
            }
        }
    }

    @Test
    void test(@TempDir final Path tempDir) throws Exception {
        final String prefix = "e2e/";
        final String key = prefix + UUID.randomUUID();
        final FileReference ref = new FileReference(BUCKET, key);

        // Initially no file
        assertThat(s3OutPort.fileExists(ref)).isFalse();

        // Save from InputStream
        final byte[] data = "hello from test".getBytes(StandardCharsets.UTF_8);
        s3OutPort.saveFile(ref, new ByteArrayInputStream(data), data.length);
        assertThat(s3OutPort.fileExists(ref)).isTrue();

        // Get metadata
        final FileMetadata meta = s3OutPort.getFileMetadata(ref);
        assertThat(meta.path()).isEqualTo(key);
        assertThat(meta.contentLength()).isEqualTo(data.length);

        final Map<String, String> sourceTags = Map.of("document-type", "invoice", "tenant", "muc");
        s3OutPort.setTags(ref, sourceTags);
        assertThat(s3OutPort.getTags(ref)).containsExactlyInAnyOrderEntriesOf(sourceTags);

        // Read content
        try (InputStream is = s3OutPort.getFileContent(ref)) {
            assertThat(is.readAllBytes()).isEqualTo(data);
        }

        final FileReference refUnknown = saveUnknownLengthFile(prefix);
        final String keyUnknown = refUnknown.path();

        // Save via File overload
        final Path p = tempDir.resolve("inports-file.txt");
        Files.writeString(p, "filecontent");
        final FileReference ref2 = new FileReference(BUCKET, key + "-file");
        s3OutPort.saveFile(ref2, p.toFile());
        assertThat(s3OutPort.fileExists(ref2)).isTrue();

        // Presigned URL (GET) and download
        final PresignedUrl pre = s3OutPort.getPresignedUrl(ref2, PresignedUrl.Action.GET, Duration.ofMinutes(2));
        try (InputStream is = pre.url().openStream()) {
            final String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(s).isEqualTo("filecontent");
        }

        // Copy with tags
        final FileReference copiedRef = new FileReference(BUCKET, key + "-copy");
        s3OutPort.copyFile(ref, copiedRef);
        assertThat(s3OutPort.fileExists(copiedRef)).isTrue();
        assertThat(s3OutPort.getTags(copiedRef)).containsExactlyInAnyOrderEntriesOf(sourceTags);
        try (InputStream is = s3OutPort.getFileContent(copiedRef)) {
            assertThat(is.readAllBytes()).isEqualTo(data);
        }

        // Copy without tags
        final FileReference copiedWithOverrideRef = new FileReference(BUCKET, key + "-copy-tagged");
        s3OutPort.copyFile(ref, copiedWithOverrideRef, false);
        assertThat(s3OutPort.fileExists(copiedWithOverrideRef)).isTrue();
        assertThat(s3OutPort.getTags(copiedWithOverrideRef)).isEmpty();
        try (InputStream is = s3OutPort.getFileContent(copiedWithOverrideRef)) {
            assertThat(is.readAllBytes()).isEqualTo(data);
        }

        // Delete
        s3OutPort.deleteFile(ref);
        s3OutPort.deleteFile(ref2);
        s3OutPort.deleteFile(copiedRef);
        s3OutPort.deleteFile(copiedWithOverrideRef);
        s3OutPort.deleteFile(new FileReference(BUCKET, keyUnknown));
        assertThat(s3OutPort.fileExists(ref)).isFalse();
        assertThat(s3OutPort.fileExists(ref2)).isFalse();
        assertThat(s3OutPort.fileExists(copiedRef)).isFalse();
        assertThat(s3OutPort.fileExists(copiedWithOverrideRef)).isFalse();
        assertThat(s3OutPort.fileExists(new FileReference(BUCKET, keyUnknown))).isFalse();
    }

    private FileReference saveUnknownLengthFile(final String prefix) throws Exception {
        final String key = prefix + UUID.randomUUID() + "-unknown";
        final FileReference reference = new FileReference(BUCKET, key);
        final int size = 6 * 1024 * 1024 + 123;
        final byte[] data = new byte[size];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        s3OutPort.saveFile(reference, new ByteArrayInputStream(data));
        assertThat(s3OutPort.fileExists(reference)).isTrue();
        assertThat(s3OutPort.getFileMetadata(reference).contentLength()).isEqualTo(data.length);
        try (InputStream is = s3OutPort.getFileContent(reference)) {
            assertThat(is.readAllBytes()).isEqualTo(data);
        }
        return reference;
    }

    @Test
    void listDirectoryFiles_nonRecursive_returnsCommonPrefixes() throws Exception {
        final String dirPrefix = "e2e-listing/" + UUID.randomUUID() + "/";
        final FileReference refDir1 = new FileReference(BUCKET, dirPrefix + "file1.txt");
        final FileReference refDir2 = new FileReference(BUCKET, dirPrefix + "subdir/file2.txt");
        final FileReference refDir3 = new FileReference(BUCKET, dirPrefix + "file3.txt");

        try {
            s3OutPort.saveFile(refDir1, new ByteArrayInputStream("f1".getBytes(StandardCharsets.UTF_8)), 2);
            s3OutPort.saveFile(refDir2, new ByteArrayInputStream("f2".getBytes(StandardCharsets.UTF_8)), 2);
            s3OutPort.saveFile(refDir3, new ByteArrayInputStream("f3".getBytes(StandardCharsets.UTF_8)), 2);

            final ListResult recursiveList = s3OutPort.getFiles(BUCKET, dirPrefix, true)
                    .iterator()
                    .next();
            assertThat(recursiveList.files()).extracting(FileMetadata::path)
                    .containsExactlyInAnyOrder(refDir1.path(), refDir2.path(), refDir3.path());
            assertThat(recursiveList.commonPrefixes()).isEmpty();

            final ListResult nonRecursiveList = s3OutPort.getFiles(BUCKET, dirPrefix, false)
                    .iterator()
                    .next();
            assertThat(nonRecursiveList.files()).extracting(FileMetadata::path)
                    .containsExactlyInAnyOrder(refDir1.path(), refDir3.path());
            assertThat(nonRecursiveList.commonPrefixes()).containsExactly(dirPrefix + "subdir/");
        } finally {
            s3OutPort.deleteFile(refDir1);
            s3OutPort.deleteFile(refDir2);
            s3OutPort.deleteFile(refDir3);
        }
    }
}
