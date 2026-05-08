package de.muenchen.oss.refarch.integration.email.domain.model;

import jakarta.activation.DataSource;

public record Attachment(
        String fileName,
        DataSource file) {
}
