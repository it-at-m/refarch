package de.muenchen.oss.refarch.integration.dms.domain.model;

import jakarta.validation.constraints.NotBlank;

public record Procedure(String coo, @NotBlank String fileCOO, @NotBlank String title, String fileSubj) {
    public Procedure(final String fileCOO, final String title, final String fileSubj) {
        this(null, fileCOO, title, fileSubj);
    }
}
