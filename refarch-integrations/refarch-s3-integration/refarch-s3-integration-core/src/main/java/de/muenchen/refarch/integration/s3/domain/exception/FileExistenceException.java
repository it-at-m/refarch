package de.muenchen.refarch.integration.s3.domain.exception;

import java.io.Serial;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileExistenceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public FileExistenceException(final String message) {
        super(message);
    }
}
