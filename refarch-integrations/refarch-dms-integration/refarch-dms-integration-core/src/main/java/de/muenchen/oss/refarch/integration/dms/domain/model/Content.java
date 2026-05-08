package de.muenchen.oss.refarch.integration.dms.domain.model;

public record Content(String extension, String name, byte[] content) {

    public byte[] content() {
        return content.clone();
    }

}
