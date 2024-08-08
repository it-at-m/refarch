package de.muenchen.refarch.s3.integration.domain.model;

public record FileData(
        String pathToFile,
        Integer expiresInMinutes) {

    public static final int LENGTH_PATH_TO_FILE = 1024;
    public static final int MIN_EXPIRES_IN_MINUTES = 1;
    public FileData() {
        this(null, null);
    }
}
