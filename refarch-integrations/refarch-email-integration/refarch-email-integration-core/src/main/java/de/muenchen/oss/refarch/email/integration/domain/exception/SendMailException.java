package de.muenchen.oss.refarch.email.integration.domain.exception;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class SendMailException extends RuntimeException {
    public SendMailException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
