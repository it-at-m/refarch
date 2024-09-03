package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;

import java.util.List;

public interface UpdateDocumentInPort {

    DocumentResponse updateDocument(String documentCOO, String user, DocumentType type, List<String> filepaths, String fileContext, String processDefinition);

}
