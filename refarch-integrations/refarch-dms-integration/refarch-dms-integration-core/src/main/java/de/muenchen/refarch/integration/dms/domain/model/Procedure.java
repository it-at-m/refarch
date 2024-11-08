package de.muenchen.refarch.integration.dms.domain.model;

public record Procedure(String coo, String fileCOO, String title, String fileSubj) {
    public Procedure(final String fileCOO, final String title, final String fileSubj) {
        this(null, fileCOO, title, fileSubj);
    }
}
