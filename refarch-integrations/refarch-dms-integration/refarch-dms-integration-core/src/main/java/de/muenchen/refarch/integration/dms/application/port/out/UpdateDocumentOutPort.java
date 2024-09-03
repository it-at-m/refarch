package de.muenchen.refarch.integration.dms.application.port.out;

import de.muenchen.refarch.integration.dms.domain.Content;
import de.muenchen.refarch.integration.dms.domain.DocumentType;

import java.util.List;

public interface UpdateDocumentOutPort {

    void updateDocument(String documentCOO, DocumentType type, List<Content> contents, String user);

}
