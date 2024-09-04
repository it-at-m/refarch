package de.muenchen.refarch.integration.dms.domain.model;

public record Procedure(String coo, String fileCOO, String title, String fileSubj) {
    public Procedure(String fileCOO, String title, String fileSubj) {
        this(null, fileCOO, title, fileSubj);
    }
}
