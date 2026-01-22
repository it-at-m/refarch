package de.muenchen.refarch.integration.s3.adapter.out.s3;

import de.muenchen.refarch.integration.s3.domain.model.FileMetadata;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3Mapper {
    FileMetadata toDomain(HeadObjectResponse response, String path) {
        return new FileMetadata(path, response.contentLength(), response.eTag(), response.lastModified());
    }

    FileMetadata toDomain(S3Object s3Object) {
        return new FileMetadata(s3Object.key(), s3Object.size(), s3Object.eTag(), s3Object.lastModified());
    }
}
