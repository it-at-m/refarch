package de.muenchen.refarch.integration.s3;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Adapter;
import de.muenchen.refarch.integration.s3.adapter.out.s3.S3Mapper;
import de.muenchen.refarch.integration.s3.application.port.in.FileOperationsInPort;
import de.muenchen.refarch.integration.s3.application.port.in.FolderOperationsInPort;
import de.muenchen.refarch.integration.s3.application.usecase.FileOperationsUseCase;
import de.muenchen.refarch.integration.s3.application.usecase.FolderOperationsUseCase;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
class S3InPortsE2ETest {

    private static final String ACCESS_KEY = "minio";
    private static final String SECRET_KEY = "Test1234";
    private static final String BUCKET = "test-bucket";

    @Container
    private static final GenericContainer<?> MINIO = new GenericContainer<>("quay.io/minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server", "/data", "--console-address", ":9001")
            .withExposedPorts(9000, 9001);

    private FileOperationsInPort fileOps;
    private FolderOperationsInPort folderOps;

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
                .build();

        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {
        }

        final S3Mapper mapper = new S3Mapper();
        final S3Adapter s3Adapter = new S3Adapter(mapper, s3Client, s3Presigner);

        this.fileOps = new FileOperationsUseCase(s3Adapter);
        this.folderOps = new FolderOperationsUseCase(s3Adapter);
    }

    @Test
    void test(@TempDir final Path tempDir) throws Exception {
        final String prefix = "e2e/";
        final String key = prefix + UUID.randomUUID();
        final FileReference ref = new FileReference(BUCKET, key);

        // Initially no file
        assertThat(fileOps.fileExists(ref)).isFalse();

        // Save from InputStream
        final byte[] data = "hello from inports".getBytes(StandardCharsets.UTF_8);
        fileOps.saveFile(ref, new java.io.ByteArrayInputStream(data), data.length);
        assertThat(fileOps.fileExists(ref)).isTrue();

        // Get metadata
        final FileMetadata meta = fileOps.getFileMetadata(ref);
        assertThat(meta.path()).isEqualTo(key);
        assertThat(meta.contentLength()).isEqualTo((long) data.length);

        // Read content
        try (InputStream is = fileOps.getFileContent(ref)) {
            assertThat(is.readAllBytes()).isEqualTo(data);
        }

        // Save via File overload
        final Path p = tempDir.resolve("inports-file.txt");
        Files.writeString(p, "filecontent");
        final FileReference ref2 = new FileReference(BUCKET, key + "-file");
        fileOps.saveFile(ref2, p.toFile());
        assertThat(fileOps.fileExists(ref2)).isTrue();

        // Presigned URL (GET) and download
        //        final PresignedUrl pre = fileOps.getPresignedUrl(ref2, PresignedUrl.Action.GET, Duration.ofMinutes(2));
        //        try (InputStream is = pre.url().openStream()) {
        //            final String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        //            assertThat(s).isEqualTo("filecontent");
        //        }

        // List via folder ops
        final List<FileMetadata> listed = folderOps.getFilesInFolder(BUCKET, prefix, true);
        assertThat(listed.stream().map(FileMetadata::path)).anyMatch(k -> k.equals(key) || k.equals(key + "-file"));

        // Delete
        fileOps.deleteFile(ref);
        fileOps.deleteFile(ref2);
        assertThat(fileOps.fileExists(ref)).isFalse();
        assertThat(fileOps.fileExists(ref2)).isFalse();
    }
}
