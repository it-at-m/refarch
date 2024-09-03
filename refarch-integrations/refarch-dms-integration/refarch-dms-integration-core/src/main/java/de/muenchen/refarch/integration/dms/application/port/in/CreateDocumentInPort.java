package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;

import java.time.LocalDate;
import java.util.List;

public interface CreateDocumentInPort {

    DocumentResponse createDocument(final String procedureCOO, final String title, final LocalDate date, final String user, DocumentType type,
                                    final List<String> filepaths, final String fileContext, final String processDefinition);

}
