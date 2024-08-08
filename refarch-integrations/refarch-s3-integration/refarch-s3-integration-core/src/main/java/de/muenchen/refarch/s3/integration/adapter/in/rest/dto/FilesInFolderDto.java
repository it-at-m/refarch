package de.muenchen.refarch.s3.integration.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "File paths")
public record FilesInFolderDto(
        Set<String> pathToFiles
) {
}
