package de.muenchen.refarch.integration.s3.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record FileMetadata(
        @NotBlank String path,
        @NotNull Long contentLength,
        @NotBlank String eTag,
        @NotNull Instant lastModified) {
}
