package de.muenchen.oss.refarch.integration.s3.domain.model;

import jakarta.validation.constraints.NotBlank;

public record FileReference(
        @NotBlank String bucket,
        @NotBlank String path) {
}
