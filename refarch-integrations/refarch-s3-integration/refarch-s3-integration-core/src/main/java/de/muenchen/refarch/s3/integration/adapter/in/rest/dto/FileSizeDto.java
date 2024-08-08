package de.muenchen.refarch.s3.integration.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "File path to size")
public record FileSizeDto(
        long fileSize) {
}
