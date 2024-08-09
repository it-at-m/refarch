package de.muenchen.refarch.integration.s3.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned file url")
public record PresignedUrlDto(
        String url,
        String path,
        String action) {
}
