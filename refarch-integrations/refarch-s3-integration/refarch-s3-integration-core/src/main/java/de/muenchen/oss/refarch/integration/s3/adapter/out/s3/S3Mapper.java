package de.muenchen.oss.refarch.integration.s3.adapter.out.s3;

import de.muenchen.oss.refarch.integration.s3.domain.model.FileMetadata;
import de.muenchen.oss.refarch.integration.s3.domain.model.ListResult;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Mapper {
    protected ListResult toDomain(final ListObjectsV2Response response) {
        return new ListResult(
                response.contents().stream()
                        .map(this::toDomain)
                        .toList(),
                response.commonPrefixes().stream()
                        .map(CommonPrefix::prefix)
                        .toList(),
                Boolean.TRUE.equals(response.isTruncated()));
    }

    protected FileMetadata toDomain(final HeadObjectResponse response, final String path) {
        return new FileMetadata(path, response.contentLength(), response.eTag(), response.lastModified());
    }

    protected FileMetadata toDomain(final S3Object s3Object) {
        return new FileMetadata(s3Object.key(), s3Object.size(), s3Object.eTag(), s3Object.lastModified());
    }
}
