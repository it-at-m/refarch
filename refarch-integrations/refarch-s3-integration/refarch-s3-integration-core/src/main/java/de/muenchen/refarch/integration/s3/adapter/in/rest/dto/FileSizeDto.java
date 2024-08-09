package de.muenchen.refarch.integration.s3.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "File path to size")
public record FileSizeDto(
        long fileSize) {
}
