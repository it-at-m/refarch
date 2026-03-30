package de.muenchen.refarch.integration.s3;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Mapper;
import de.muenchen.refarch.integration.s3.adapter.out.s3.S3OutAdapter;
import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
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
        this.s3OutPort = new S3OutAdapter(mapper, s3Client, s3Presigner);
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
        s3OutPort.saveFile(ref, new java.io.ByteArrayInputStream(data), data.length);
        assertThat(s3OutPort.fileExists(ref)).isTrue();

        // Get metadata
        final FileMetadata meta = s3OutPort.getFileMetadata(ref);
        assertThat(meta.path()).isEqualTo(key);
        assertThat(meta.contentLength()).isEqualTo(data.length);

        // Read content
        try (InputStream is = s3OutPort.getFileContent(ref)) {
            assertThat(is.readAllBytes()).isEqualTo(data);
        }

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

        // Prepare directory-like structure under e2e/dir for recursive vs non-recursive listing
        final String dirPrefix = prefix + "dir/";
        final FileReference refDir1 = new FileReference(BUCKET, dirPrefix + "file1.txt");
        final FileReference refDir2 = new FileReference(BUCKET, dirPrefix + "subdir/file2.txt");
        final FileReference refDir3 = new FileReference(BUCKET, dirPrefix + "file3.txt");
        s3OutPort.saveFile(refDir1, new java.io.ByteArrayInputStream("f1".getBytes(StandardCharsets.UTF_8)), 2);
        s3OutPort.saveFile(refDir2, new java.io.ByteArrayInputStream("f2".getBytes(StandardCharsets.UTF_8)), 2);
        s3OutPort.saveFile(refDir3, new java.io.ByteArrayInputStream("f3".getBytes(StandardCharsets.UTF_8)), 2);

        // List via folder ops (recursive)
        final List<FileMetadata> listed = s3OutPort.getFilesWithPrefix(BUCKET, prefix, true);
        assertThat(listed.stream().map(FileMetadata::path)).anyMatch(k -> k.equals(key) || k.equals(key + "-file"));

        // Recursive listing should include immediate and nested children
        final List<FileMetadata> recursiveList = s3OutPort.getFilesWithPrefix(BUCKET, dirPrefix, true, 1000, null);
        assertThat(recursiveList).extracting(FileMetadata::path)
                .containsExactlyInAnyOrder(dirPrefix + "file1.txt", dirPrefix + "subdir/file2.txt", dirPrefix + "file3.txt");

        // Non-recursive listing should only include immediate children (delimiter "/" behavior)
        final List<FileMetadata> nonRecursiveList = s3OutPort.getFilesWithPrefix(BUCKET, dirPrefix, false, 1000, null);
        assertThat(nonRecursiveList).extracting(FileMetadata::path)
                .containsExactlyInAnyOrder(dirPrefix + "file1.txt", dirPrefix + "file3.txt");

        // Delete
        s3OutPort.deleteFile(ref);
        s3OutPort.deleteFile(ref2);
        s3OutPort.deleteFile(refDir1);
        s3OutPort.deleteFile(refDir2);
        s3OutPort.deleteFile(refDir3);
        assertThat(s3OutPort.fileExists(ref)).isFalse();
        assertThat(s3OutPort.fileExists(ref2)).isFalse();
        assertThat(s3OutPort.fileExists(refDir1)).isFalse();
        assertThat(s3OutPort.fileExists(refDir2)).isFalse();
        assertThat(s3OutPort.fileExists(refDir3)).isFalse();
    }
}
