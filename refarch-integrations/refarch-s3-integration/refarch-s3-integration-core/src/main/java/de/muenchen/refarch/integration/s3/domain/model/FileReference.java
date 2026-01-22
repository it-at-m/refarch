package de.muenchen.refarch.integration.s3.domain.model;

import jakarta.validation.constraints.NotBlank;

public record FileReference(
        @NotBlank String bucket,
        @NotBlank String path
) {
}
