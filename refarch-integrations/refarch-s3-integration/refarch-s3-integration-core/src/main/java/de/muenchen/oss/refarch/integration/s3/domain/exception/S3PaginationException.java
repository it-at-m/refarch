package de.muenchen.oss.refarch.integration.s3.domain.exception;

/**
 * Indicates that a S3 pagination request failed.
 */
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class S3PaginationException extends RuntimeException {
    public S3PaginationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public S3PaginationException(final String message) {
        super(message);
    }
}
