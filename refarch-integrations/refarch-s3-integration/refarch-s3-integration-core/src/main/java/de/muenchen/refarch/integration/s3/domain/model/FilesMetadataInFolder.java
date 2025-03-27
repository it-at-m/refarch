package de.muenchen.refarch.integration.s3.domain.model;

import java.util.Collections;
import java.util.List;

public record FilesMetadataInFolder(
        List<FileMetadata> files) {
    public FilesMetadataInFolder {
        files = List.copyOf(files);
    }

    @Override
    public List<FileMetadata> files() {
        return Collections.unmodifiableList(files);
    }
}
