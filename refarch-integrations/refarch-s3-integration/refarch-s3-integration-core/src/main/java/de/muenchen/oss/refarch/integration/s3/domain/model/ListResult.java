package de.muenchen.oss.refarch.integration.s3.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ListResult(
        @NotNull List<@Valid FileMetadata> files,
        @NotNull List<String> commonPrefixes,
        boolean truncated,
        String nextMarker) {
}
