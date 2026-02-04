package de.muenchen.refarch.integration.s3.adapter.out.s3;

import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Mapper {
    public FileMetadata toDomain(final HeadObjectResponse response, final String path) { // PMD: CommentDefaultAccessModifier
        return new FileMetadata(path, response.contentLength(), response.eTag(), response.lastModified());
    }

    public FileMetadata toDomain(final S3Object s3Object) { // PMD: CommentDefaultAccessModifier
        return new FileMetadata(s3Object.key(), s3Object.size(), s3Object.eTag(), s3Object.lastModified());
    }
}
