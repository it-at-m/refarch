package de.muenchen.refarch.integration.s3.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreatePresignedUrlEvent(
        @Pattern(regexp = "GET|PUT|DELETE|POST") String action,
        @NotNull(message = "path is mandatory") @NotBlank(message = "path is mandatory") String path) {
}
