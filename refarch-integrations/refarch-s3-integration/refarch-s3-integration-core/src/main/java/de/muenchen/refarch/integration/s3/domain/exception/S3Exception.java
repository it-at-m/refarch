package de.muenchen.refarch.integration.s3.domain.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Represents a technical exception
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class S3Exception extends Exception {
    public S3Exception(final String message) {
        super(message);
    }

    public S3Exception(final String message, final Exception exception) {
        super(message, exception);
    }

}
