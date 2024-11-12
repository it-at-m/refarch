package de.muenchen.refarch.integration.dms.domain.model;

import java.time.LocalDate;
import java.util.List;

public record Document(String procedureCOO, String title, LocalDate date, DocumentType type, List<Content> contents) {

}
