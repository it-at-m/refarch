package de.muenchen.refarch.s3.integration.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "File paths to sizes")
public record FileSizesInFolderDto(
        Map<String, Long> fileSizes) {
}
