package de.muenchen.refarch.s3.integration.domain.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Represents a technical exception
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileSystemAccessException extends Exception {
    public FileSystemAccessException(final String message) {
        super(message);
    }

    public FileSystemAccessException(final String message, final Exception exception) {
        super(message, exception);
    }

}
