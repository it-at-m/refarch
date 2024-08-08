package de.muenchen.refarch.s3.integration.domain.model;

public record PresignedUrl(
        String url,
        String path,
        String action) {
}
