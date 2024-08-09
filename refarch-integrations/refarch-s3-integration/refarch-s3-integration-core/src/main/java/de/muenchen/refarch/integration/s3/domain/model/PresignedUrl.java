package de.muenchen.refarch.integration.s3.domain.model;

public record PresignedUrl(
        String url,
        String path,
        String action) {
}
