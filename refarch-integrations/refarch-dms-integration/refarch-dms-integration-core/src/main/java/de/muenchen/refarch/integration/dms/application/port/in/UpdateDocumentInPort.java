package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.DocumentType;

import java.util.List;

public interface UpdateDocumentInPort {

    DocumentResponse updateDocument(String documentCOO, String user, DocumentType type, List<String> filepaths, String fileContext, String processDefinition);

}
