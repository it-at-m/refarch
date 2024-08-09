package de.muenchen.refarch.integration.s3.domain.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileExistenceException extends RuntimeException {
    public FileExistenceException(final String message) {
        super(message);
    }
}
