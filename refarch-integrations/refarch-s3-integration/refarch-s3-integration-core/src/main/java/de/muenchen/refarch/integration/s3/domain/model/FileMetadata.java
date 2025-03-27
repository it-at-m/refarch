package de.muenchen.refarch.integration.s3.domain.model;

import java.time.LocalDateTime;

public record FileMetadata(
        String pathToFile,
        Long fileSize,
        String etag,
        LocalDateTime lastModified) {

    public FileMetadata() {
        this(null, null, null, null);
    }
}
