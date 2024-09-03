package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;

import java.util.List;

public interface UpdateDocumentOutPort {

    void updateDocument(String documentCOO, DocumentType type, List<Content> contents, String user);

}
