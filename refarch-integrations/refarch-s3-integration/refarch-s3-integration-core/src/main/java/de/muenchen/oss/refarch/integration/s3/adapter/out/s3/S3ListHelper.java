package de.muenchen.oss.refarch.integration.s3.adapter.out.s3;

import de.muenchen.oss.refarch.integration.s3.domain.exception.S3Exception;
import de.muenchen.oss.refarch.integration.s3.domain.exception.S3PaginationException;
import de.muenchen.oss.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.oss.refarch.integration.s3.domain.model.ListResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

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
            final ListObjectsV2Request request = buildRequest(bucket, prefix, recursive, maxKeys, startAfter, null);
            return s3Mapper.toDomain(s3Client.listObjectsV2(request));
        } catch (final SdkException e) {
            throw new S3Exception("Error while listing (bucket: %s, path: %s, maxKeys: %d, startAfter: %s)"
                    .formatted(bucket, prefix, maxKeys, startAfter), e);
        }
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    protected Iterable<ListResult> getAllPages(
            final String bucket,
            final String prefix,
            final boolean recursive,
            final int maxKeys,
            final String startAfter) {
        return () -> new Iterator<>() {
            private String continuationToken;
            private boolean firstPage = true;
            private boolean finished;
            private boolean nextPageLoaded;
            private ListResult nextPage;

            @Override
            public boolean hasNext() {
                if (!nextPageLoaded && !finished) {
                    nextPage = fetchNextPage();
                    nextPageLoaded = true;
                }
                return nextPageLoaded;
            }

            @Override
            public ListResult next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final ListResult page = nextPage;
                nextPageLoaded = false;
                return page;
            }

            private ListResult fetchNextPage() {
                try {
                    final ListObjectsV2Request request = buildRequest(
                            bucket, prefix, recursive, maxKeys, firstPage ? startAfter : null, continuationToken);
                    final ListObjectsV2Response response = s3Client.listObjectsV2(request);
                    firstPage = false;
                    continuationToken = response.nextContinuationToken();
                    finished = !Boolean.TRUE.equals(response.isTruncated());
                    if (!finished && continuationToken == null) {
                        throw new S3PaginationException("Truncated response but no continuationToken");
                    }
                    return s3Mapper.toDomain(response);
                } catch (final SdkException e) {
                    throw new S3PaginationException(
                            "Error while listing (bucket: %s, path: %s, maxKeys: %d, startAfter: %s)"
                                    .formatted(bucket, prefix, maxKeys, startAfter),
                            e);
                }
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
            final String startAfter,
            final String continuationToken) {
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
        if (continuationToken != null) {
            builder.continuationToken(continuationToken);
        }
        return builder.build();
    }
}
