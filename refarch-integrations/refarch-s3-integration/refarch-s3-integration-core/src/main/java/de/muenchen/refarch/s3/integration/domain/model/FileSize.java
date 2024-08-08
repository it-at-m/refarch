package de.muenchen.refarch.s3.integration.domain.model;

public record FileSize(
        long fileSize) {
    public static final int LENGTH_PATH_TO_FILE = 1024;
}
