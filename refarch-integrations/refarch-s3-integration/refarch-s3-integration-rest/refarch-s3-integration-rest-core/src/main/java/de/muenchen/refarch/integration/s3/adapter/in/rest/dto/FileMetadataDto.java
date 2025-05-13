package de.muenchen.refarch.integration.s3.adapter.in.rest.dto;

import java.time.LocalDateTime;

public record FileMetadataDto(
        String pathToFile,
        Long fileSize,
        String etag,
        LocalDateTime lastModified) {

    public FileMetadataDto() {
        this(null, null, null, null);
    }
}
