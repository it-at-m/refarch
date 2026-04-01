package de.muenchen.oss.refarch.email.integration.domain.model;

import jakarta.activation.DataSource;

public record Attachment(
        String fileName,
        DataSource file) {
}
