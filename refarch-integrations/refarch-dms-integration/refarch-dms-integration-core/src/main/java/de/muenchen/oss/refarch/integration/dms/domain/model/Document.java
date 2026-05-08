package de.muenchen.oss.refarch.integration.dms.domain.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record Document(String procedureCOO, String title, LocalDate date, DocumentType type, List<Content> contents) {

    public List<Content> contents() {
        return Collections.unmodifiableList(this.contents);
    }

}
