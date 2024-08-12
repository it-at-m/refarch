package de.muenchen.refarch.integration.s3.domain.model;

public record FileSize(
        long fileSize) {
    public static final int LENGTH_PATH_TO_FILE = 1024;
}
