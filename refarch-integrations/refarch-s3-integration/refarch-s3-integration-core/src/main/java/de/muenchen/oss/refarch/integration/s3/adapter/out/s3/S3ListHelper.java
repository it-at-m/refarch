package de.muenchen.oss.refarch.integration.s3.adapter.out.s3;

import de.muenchen.oss.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.oss.refarch.integration.s3.domain.exception.S3PaginationException;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.oss.refarch.integration.s3.domain.model.ListResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

@Component
@AllArgsConstructor
public class S3ListHelper {
    private final S3Client s3Client;
    private final S3Mapper s3Mapper;

    protected ListResult getPage(
            final String bucket,
            final String prefix,
            final boolean recursive,
            final int maxKeys,
            final String startAfter)
            throws S3Exception {
        try {
            final ListObjectsV2Request request = buildRequest(bucket, prefix, recursive, maxKeys, startAfter);
            return s3Mapper.toDomain(s3Client.listObjectsV2(request));
        } catch (final SdkException e) {
            throw new S3Exception("Error while listing (bucket: %s, path: %s, maxKeys: %d, startAfter: %s)"
                    .formatted(bucket, prefix, maxKeys, startAfter), e);
        }
    }

    protected Iterable<ListResult> getAllPages(
            final String bucket,
            final String prefix,
            final boolean recursive,
            final int maxKeys,
            final String startAfter) {
        final ListObjectsV2Request request = buildRequest(bucket, prefix, recursive, maxKeys, startAfter);
        final ListObjectsV2Iterable pages = s3Client.listObjectsV2Paginator(request);
        final String errorMessage = "Error while listing (bucket: %s, path: %s, maxKeys: %d, startAfter: %s)"
                .formatted(bucket, prefix, maxKeys, startAfter);

        return () -> new Iterator<>() {
            private final Iterator<ListObjectsV2Response> responses = pages.iterator();

            @Override
            public boolean hasNext() {
                try {
                    return responses.hasNext();
                } catch (final SdkException e) {
                    throw new S3PaginationException(errorMessage, e);
                }
            }

            @Override
            public ListResult next() {
                final ListObjectsV2Response response;
                try {
                    response = responses.next();
                } catch (final SdkException e) {
                    throw new S3PaginationException(errorMessage, e);
                }
                return s3Mapper.toDomain(response);
            }
        };
    }

    protected ListResult getAllPagesAsListResult(
            final String bucket,
            final String prefix,
            final boolean recursive,
            final int maxKeys,
            final String startAfter) {
        final List<FileMetadata> files = new ArrayList<>();
        final List<String> commonPrefixes = new ArrayList<>();
        getAllPages(bucket, prefix, recursive, maxKeys, startAfter)
                .forEach(page -> {
                    files.addAll(page.files());
                    commonPrefixes.addAll(page.commonPrefixes());
                });
        return new ListResult(files, commonPrefixes, false);
    }

    private static ListObjectsV2Request buildRequest(
            final String bucket,
            final String prefix,
            final boolean recursive,
            final int maxKeys,
            final String startAfter) {
        final ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .maxKeys(maxKeys);
        if (!recursive) {
            builder.delimiter("/");
        }
        if (startAfter != null && !startAfter.isEmpty()) {
            builder.startAfter(startAfter);
        }
        return builder.build();
    }
}
