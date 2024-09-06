package de.muenchen.refarch.integration.s3.domain.exception;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Represents a technical exception
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileSystemAccessException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileSystemAccessException(final String message) {
        super(message);
    }

    public FileSystemAccessException(final String message, final Exception exception) {
        super(message, exception);
    }

}
