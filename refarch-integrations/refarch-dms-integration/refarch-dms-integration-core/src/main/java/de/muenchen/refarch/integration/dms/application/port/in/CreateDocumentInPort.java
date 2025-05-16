package de.muenchen.refarch.integration.dms.application.port.in;

import de.muenchen.refarch.integration.dms.domain.exception.DmsException;
import de.muenchen.refarch.integration.dms.domain.model.Content;
import de.muenchen.refarch.integration.dms.domain.model.DocumentResponse;
import de.muenchen.refarch.integration.dms.domain.model.DocumentType;
import java.time.LocalDate;
import java.util.List;

public interface CreateDocumentInPort {

    DocumentResponse createDocument(String procedureCOO, String title, LocalDate date, String user, DocumentType type,
            List<Content> contents) throws DmsException;

}
