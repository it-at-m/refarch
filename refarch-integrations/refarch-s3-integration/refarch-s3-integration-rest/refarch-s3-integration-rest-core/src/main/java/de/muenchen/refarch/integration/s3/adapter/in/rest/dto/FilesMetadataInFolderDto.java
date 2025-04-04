package de.muenchen.refarch.integration.s3.adapter.in.rest.dto;

import java.util.Collections;
import java.util.List;

public record FilesMetadataInFolderDto(
        List<FileMetadataDto> files) {
    public FilesMetadataInFolderDto {
        files = List.copyOf(files);
    }

    @Override
    public List<FileMetadataDto> files() {
        return Collections.unmodifiableList(files);
    }
}
