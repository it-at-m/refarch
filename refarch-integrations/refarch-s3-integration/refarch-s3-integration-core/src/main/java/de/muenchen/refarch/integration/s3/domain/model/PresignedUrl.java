package de.muenchen.refarch.integration.s3.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URL;

public record PresignedUrl(
        @NotNull URL url,
        @NotBlank String path,
        @NotNull Action action) {

    public enum Action {
        GET,
        PUT,
        DELETE,
        HEAD
    }
}
