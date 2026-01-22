package de.muenchen.refarch.integration.s3.adapter.out.s3;

import de.muenchen.refarch.integration.s3.application.port.out.S3OutPort;
import de.muenchen.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.domain.model.FileReference;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class S3Adapter implements S3OutPort {
    private final S3Mapper s3Mapper;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public boolean fileExists(final FileReference fileReference) throws S3Exception {
        try {
            this.getFileMetadata(fileReference);
        } catch (final S3Exception e) {
            if (e.getCause() instanceof NoSuchKeyException) {
                return false;
            }
            throw e;
        }
        return true;
    }

    /**
     * Base method to store content at the given file reference using a prepared RequestBody.
     * Other saveFile overloads delegate to this method.
     */
    private void saveFile(final FileReference fileReference, final RequestBody requestBody) throws S3Exception {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(fileReference.bucket())
                    .key(fileReference.path())
                    .build(),
                requestBody);
        } catch (final SdkException e) {
            throw new S3Exception("Error while uploading %s".formatted(fileReference), e);
        }
    }

    @Override
    public void saveFile(final FileReference fileReference, final InputStream content, long contentLength) throws S3Exception {
        saveFile(fileReference, RequestBody.fromInputStream(content, contentLength));
    }

    @Override
    public void saveFile(final FileReference fileReference, final File file) throws S3Exception {
        saveFile(fileReference, RequestBody.fromFile(file));
    }

    @Override
    public FileMetadata getFileMetadata(final FileReference fileReference) throws S3Exception {
        try {
            final HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(fileReference.bucket())
                        .key(fileReference.path()).build());
            return s3Mapper.toDomain(response, fileReference.path());
        } catch (final SdkException e) {
            throw new S3Exception("Error while downloading %s".formatted(fileReference), e);
        }
    }

    @Override
    public PresignedUrl getPresignedUrl(final FileReference fileReference, final PresignedUrl.Action action, final Duration lifetime) throws S3Exception {
        try {
            final java.net.URL url;
            switch (action) {
                case GET -> {
                    final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(fileReference.bucket())
                            .key(fileReference.path())
                            .build();
                    final GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                            .getObjectRequest(getObjectRequest)
                            .signatureDuration(lifetime)
                            .build();
                    url = s3Presigner.presignGetObject(presignRequest).url();
                }
                case PUT -> {
                    final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(fileReference.bucket())
                            .key(fileReference.path())
                            .build();
                    final PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                            .putObjectRequest(putObjectRequest)
                            .signatureDuration(lifetime)
                            .build();
                    url = s3Presigner.presignPutObject(presignRequest).url();
                }
                case DELETE -> {
                    final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                            .bucket(fileReference.bucket())
                            .key(fileReference.path())
                            .build();
                    final DeleteObjectPresignRequest presignRequest = DeleteObjectPresignRequest.builder()
                            .deleteObjectRequest(deleteObjectRequest)
                            .signatureDuration(lifetime)
                            .build();
                    url = s3Presigner.presignDeleteObject(presignRequest).url();
                }
                case HEAD -> {
                    final HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                            .bucket(fileReference.bucket())
                            .key(fileReference.path())
                            .build();
                    final HeadObjectPresignRequest presignRequest = HeadObjectPresignRequest.builder()
                            .headObjectRequest(headObjectRequest)
                            .signatureDuration(lifetime)
                            .build();
                    url = s3Presigner.presignHeadObject(presignRequest).url();
                }
                default -> throw new IllegalArgumentException("Unsupported presigned URL action: " + action);
            }
            return new PresignedUrl(url, fileReference.path(), action);
        } catch (final SdkException e) {
            throw new S3Exception("Error while creating presigned URL for %s with action %s".formatted(fileReference, action), e);
        }
    }

    @Override
    public InputStream getFileContent(final FileReference fileReference) throws S3Exception {
        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(fileReference.bucket())
                    .key(fileReference.path()).build());
        } catch (final SdkException e) {
            throw new S3Exception("Error while downloading %s".formatted(fileReference), e);
        }
    }

    @Override
    public void deleteFile(final FileReference fileReference) throws S3Exception {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(fileReference.bucket())
                    .key(fileReference.path()).build());
        } catch (final SdkException e) {
            throw new S3Exception("Error while deleting %s".formatted(fileReference), e);
        }
    }

    @Override
    public List<FileMetadata> getFilesWithPrefix(final String bucket, final String prefix, final int maxKeys, final String marker) throws S3Exception {
        try {
            final ListObjectsRequest.Builder builder = ListObjectsRequest.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .maxKeys(maxKeys);
            if (marker != null && !marker.isEmpty()) {
                builder.marker(marker);
            }
            final ListObjectsResponse response = s3Client.listObjects(builder.build());
            return response.contents().stream()
                    .map(s3Mapper::toDomain)
                    .toList();
        } catch (final SdkException e) {
            throw new S3Exception("Error while listing (bucket: %s, path: %s, maxKeys: %d, marker: %s)".formatted(bucket, prefix, maxKeys, marker), e);
        }
    }
}
