package de.muenchen.refarch.email.model;

import jakarta.mail.util.ByteArrayDataSource;

public record FileAttachment(
        String fileName,
        ByteArrayDataSource file) {
}
